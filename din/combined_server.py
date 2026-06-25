"""
Combined Flask server: YOLO + DIN on single port 5000
"""
import os; os.environ['KMP_DUPLICATE_LIB_OK'] = 'TRUE'
import torch, numpy as np, pickle, io, traceback, hashlib, json
from PIL import Image
from flask import Flask, request, jsonify
from ultralytics import YOLO
import torchvision.transforms as T
from torchvision import models

app = Flask(__name__)
DEVICE = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

# ============================================================
# YOLO Setup
# ============================================================
MODEL_DIR = os.environ.get("MODEL_DIR", "D:/YueQian/yolo")
IMAGE_DIR = os.environ.get("IMAGE_DIR", "D:/YueQian/shopping-frontend/frontend/yuexuan-app/public/images")

COCO_MODEL = os.path.join(MODEL_DIR, "yolov8n.pt")
SNACKS_MODEL = os.path.join(MODEL_DIR, "best.pt")
print(f"[YOLO] Loading COCO model...")
coco_model = YOLO(COCO_MODEL)
print(f"[YOLO] Loading snacks model...")
snacks_model = YOLO(SNACKS_MODEL)
print(f"[YOLO] COCO={len(coco_model.names)} classes, snacks={len(snacks_model.names)} classes")

print("[YOLO] Loading MobileNetV3 feature extractor...")
backbone = models.mobilenet_v3_small(weights="DEFAULT")
backbone.classifier = torch.nn.Identity()
backbone.eval().to(DEVICE)

preprocess = T.Compose([
    T.Resize(256), T.CenterCrop(224), T.ToTensor(),
    T.Normalize(mean=[0.485,0.456,0.406], std=[0.229,0.224,0.225]),
])

PRODUCTS = [
    {"pid":1,"name":"Xiaomi 14 Ultra","price":6999,"category":"Digital","images":["xiaomi-1.png","xiaomi-2.png","xiaomi-3.png","xiaomi-4.png"]},
    {"pid":2,"name":"Huawei MatePad Pro","price":4999,"category":"Digital","images":["huawei-1.png","huawei-2.png","huawei-3.png","huawei-4.png"]},
    {"pid":3,"name":"Air Max Sports Shoes","price":899,"category":"Sports","images":["airmax-1.png","airmax-2.png","airmax-3.png","airmax-4.png"]},
    {"pid":4,"name":"Cotton T-Shirt","price":129,"category":"Clothing","images":["tshirt-1.png","tshirt-2.png","tshirt-3.png","tshirt-4.png"]},
    {"pid":5,"name":"Nut Gift Box","price":88,"category":"Food","images":["nuts-1.png","nuts-2.png","nuts-3.png","nuts-4.png"]},
    {"pid":6,"name":"Nordic Desk Lamp","price":159,"category":"Home","images":["lamp-1.png","lamp-2.png","lamp-3.png","lamp-4.png"]},
    {"pid":7,"name":"Maz Maz Tomato Chips","price":15.9,"category":"Food","images":["mazmaz番茄-1.png","mazmaz番茄-2.png","mazmaz番茄-3.png","mazmaz番茄-4.png"]},
    {"pid":8,"name":"Mini Lina Biscuits","price":12.9,"category":"Food","images":["minilina-1.png","minilina-2.png","minilina-3.png","minilina-4.png"]},
    {"pid":9,"name":"Maz Maz Potato Sticks","price":13.9,"category":"Food","images":["mazmaz土豆条-1.png","mazmaz土豆条-2.png","mazmaz土豆条-3.png","mazmaz土豆条-4.png"]},
]

COCO_CN = {"backpack":"Backpack","umbrella":"Umbrella","handbag":"Handbag","tie":"Tie","suitcase":"Suitcase","sports ball":"Ball","skateboard":"Skateboard","bottle":"Bottle","wine glass":"Glass","cup":"Cup","bowl":"Bowl","chair":"Chair","couch":"Sofa","potted plant":"Plant","bed":"Bed","dining table":"Table","tv":"TV","laptop":"Laptop","mouse":"Mouse","remote":"Remote","keyboard":"Keyboard","cell phone":"Phone","book":"Book","clock":"Clock","vase":"Vase","scissors":"Scissors","teddy bear":"Bear","hair drier":"Hair Drier","toothbrush":"Toothbrush","tennis racket":"Racket","frisbee":"Frisbee","banana":"Banana","apple":"Apple","sandwich":"Sandwich","orange":"Orange","broccoli":"Broccoli","carrot":"Carrot","hot dog":"Hot Dog","pizza":"Pizza","donut":"Donut","cake":"Cake","microwave":"Microwave","oven":"Oven","toaster":"Toaster","sink":"Sink","refrigerator":"Fridge"}

def extract_features(img):
    tensor = preprocess(img).unsqueeze(0).to(DEVICE)
    with torch.no_grad(): feats = backbone(tensor)
    return feats.squeeze(0)

def cosine_sim(a, b):
    return float(torch.nn.functional.cosine_similarity(a.unsqueeze(0), b.unsqueeze(0), dim=1))

print("[YOLO] Computing reference embeddings...")
ref_embeddings = {}
for p in PRODUCTS:
    embs = []
    for fname in p["images"]:
        fpath = os.path.join(IMAGE_DIR, fname)
        if os.path.exists(fpath):
            img = Image.open(fpath).convert("RGB")
            embs.append(extract_features(img))
    if embs:
        ref_embeddings[p["pid"]] = embs
        print(f"  [OK] {p['name']} - {len(embs)} images")
print(f"[YOLO] {len(ref_embeddings)} products ready")

# ============================================================
# DIN Setup
# ============================================================
print("\n[DIN] Loading model...")
from din_model import load_model as din_load
din_model = din_load('D:/YueQian/din/din_model.pt', device=DEVICE)
din_model.eval()

with open('D:/YueQian/din/data/encoder.pkl', 'rb') as f: encoder = pickle.load(f)
with open('D:/YueQian/din/data/user_sequences.pkl', 'rb') as f: user_sequences = pickle.load(f)
with open('D:/YueQian/din/data/p_items.pkl', 'rb') as f: p_items = pickle.load(f)

item_to_idx = encoder['item_to_idx']; cat_to_idx = encoder['cat_to_idx']; max_seq_len = encoder['max_seq_len']

p_item_ids = list(p_items.keys())
p_item_indices = np.array([item_to_idx.get(iid,0) for iid in p_item_ids], dtype=np.int64)
p_cat_indices = np.array([cat_to_idx.get(p_items[iid],0) for iid in p_item_ids], dtype=np.int64)
valid = (p_item_indices > 0) & (p_item_indices < encoder['num_items'])
p_item_indices = p_item_indices[valid].astype(np.int32)
p_cat_indices = p_cat_indices[valid].astype(np.int32)
p_item_ids_clean = [p_item_ids[i] for i in range(len(p_item_ids)) if valid[i]]

ACCOUNT_MAP = {'admin':'98047837','dafei':'97726136','shoe_shop':'98607707','cloth_shop':'98662432',
               'user1':'98047837','user':'97726136'}

def din_recommend(uid_str, top_k=10):
    uid = int(uid_str)
    if uid not in user_sequences: return {'error': f'User {uid} not found'}
    seq_data = user_sequences[uid]
    items = seq_data['items'][-max_seq_len:]
    cats = seq_data['cats'][-max_seq_len:]
    seq_i = np.zeros(max_seq_len, dtype=np.int32)
    seq_c = np.zeros(max_seq_len, dtype=np.int32)
    seq_m = np.zeros(max_seq_len, dtype=np.float32)
    for i,(iid,cid) in enumerate(zip(items,cats)):
        seq_i[i]=item_to_idx.get(iid,0); seq_c[i]=cat_to_idx.get(cid,0); seq_m[i]=1.0
    all_scores = []
    for s in range(0, len(p_item_ids_clean), 8192):
        e = min(s+8192, len(p_item_ids_clean)); n = e-s
        si = torch.LongTensor(np.tile(seq_i,(n,1))).to(DEVICE)
        sc = torch.LongTensor(np.tile(seq_c,(n,1))).to(DEVICE)
        sm = torch.FloatTensor(np.tile(seq_m,(n,1))).to(DEVICE)
        ti = torch.LongTensor(p_item_indices[s:e]).to(DEVICE)
        tc = torch.LongTensor(p_cat_indices[s:e]).to(DEVICE)
        scores = din_model.predict(si,sc,sm,ti,tc)
        all_scores.extend(scores.cpu().numpy().tolist())
    top = np.argsort(all_scores)[-top_k:][::-1]
    recs = [{'item_id':int(p_item_ids_clean[i]),'category':int(p_items.get(p_item_ids_clean[i],0)),'score':round(float(all_scores[i]),4)} for i in top]
    return {'user_id':uid,'recommendations':recs}

print(f"[DIN] {len(user_sequences)} users, {len(p_item_ids_clean)} P items loaded")

# ============================================================
# Combined Endpoints
# ============================================================

@app.route("/health")
def health():
    return jsonify({
        'yolo': {'coco':len(coco_model.names),'snacks':len(snacks_model.names),'products':len(ref_embeddings)},
        'din': {'users':len(user_sequences),'p_items':len(p_item_ids_clean)},
    })

# ---- YOLO routes ----
@app.route("/detect", methods=["POST"])
def detect():
    if "image" not in request.files:
        return jsonify({"error":"missing image"}),400
    model_choice = request.args.get("model","coco")
    file = request.files["image"]
    img = Image.open(io.BytesIO(file.read()))
    all_detections = []
    if model_choice in ("coco","both"):
        results = coco_model(img, conf=0.35, imgsz=640)
        for r in results:
            for box in r.boxes:
                cls_id = int(box.cls[0])
                en = coco_model.names.get(cls_id,"unknown")
                all_detections.append({"class_id":cls_id,"class_name":COCO_CN.get(en,en),"class_name_en":en,"confidence":round(float(box.conf[0]),4),"bbox":[round(x,2) for x in box.xyxy[0].tolist()],"source":"coco"})
    if model_choice in ("snacks","both"):
        results = snacks_model(img, conf=0.25, imgsz=640)
        for r in results:
            for box in r.boxes:
                cls_id = int(box.cls[0])
                all_detections.append({"class_id":cls_id,"class_name":snacks_model.names.get(cls_id,"unknown"),"class_name_en":snacks_model.names.get(cls_id,"unknown"),"confidence":round(float(box.conf[0]),4),"bbox":[round(x,2) for x in box.xyxy[0].tolist()],"source":"snacks"})
    all_detections.sort(key=lambda d:d["confidence"],reverse=True)
    return jsonify({"model":model_choice,"count":len(all_detections),"detections":all_detections})

@app.route("/recognize", methods=["POST"])
def recognize():
    if "image" not in request.files:
        return jsonify({"matched":False,"error":"missing image"}),400
    file = request.files["image"]
    img = Image.open(io.BytesIO(file.read())).convert("RGB")
    query_feat = extract_features(img)
    best_pid,best_sim,all_scores = None,-1.0,[]
    for pid,embs in ref_embeddings.items():
        sims = [cosine_sim(query_feat,emb) for emb in embs]
        max_sim = max(sims)
        all_scores.append({"pid":pid,"similarity":round(max_sim,4)})
        if max_sim > best_sim: best_sim,best_pid = max_sim,pid
    all_scores.sort(key=lambda x:x["similarity"],reverse=True)
    if best_sim < 0.55:
        return jsonify({"matched":False,"message":"Cannot recognize this product","top_scores":all_scores[:3]})
    product = next(p for p in PRODUCTS if p["pid"]==best_pid)
    return jsonify({"matched":True,"product":{"pid":product["pid"],"name":product["name"],"price":product["price"],"category":product["category"]},"confidence":round(best_sim,4),"method":"feature_matching","top_scores":all_scores[:3]})

# ---- DIN route ----
@app.route("/recommend", methods=["POST","GET","OPTIONS"])
def recommend():
    if request.method == "OPTIONS":
        resp = app.make_default_options_response()
        resp.headers['Access-Control-Allow-Origin'] = '*'
        resp.headers['Access-Control-Allow-Methods'] = 'GET,POST,OPTIONS'
        resp.headers['Access-Control-Allow-Headers'] = '*'
        return resp
    username = 'admin'
    try:
        data = request.get_json(silent=True)
        if data and 'username' in data: username = data['username']
    except: pass
    if request.args.get('username'): username = request.args['username']
    if username in ACCOUNT_MAP:
        uid = ACCOUNT_MAP[username]
    else:
        keys = list(ACCOUNT_MAP.keys())
        idx = int(hashlib.md5(username.encode()).hexdigest(),16) % len(keys)
        uid = ACCOUNT_MAP[keys[idx]]
    result = din_recommend(uid)
    result['username'] = username
    return jsonify(result)

print("\n" + "="*50)
print("Combined server ready on :5000")
print("YOLO: /detect /recognize")
print("DIN:  /recommend")
print("Both: /health")
print("="*50 + "\n")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
