<template>
  <div class="adm-page">
    <div class="adm-head">
      <div>
        <h2>商品管理 📦</h2>
        <span class="sub">维护商品上下架、价格库存 · 共 {{ products.length }} 件</span>
      </div>
      <el-button type="primary" size="large" :icon="Plus" @click="openAdd">新增商品</el-button>
    </div>

    <!-- 工具栏卡片 -->
    <div class="adm-card toolbar">
      <el-input
        v-model="keyword" placeholder="搜索商品名称" style="width:280px" clearable
        @clear="load" @keyup.enter="load" size="large"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select
        v-model="category" placeholder="全部类目" style="width:160px" clearable
        @change="load" size="large"
      >
        <el-option v-for="c in cats" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" size="large" :icon="Search" @click="load">搜索</el-button>
      <div class="spacer"></div>
      <span class="count-tip" v-if="keyword || category">
        关键词「<b>{{ keyword || '·' }}</b>」· 类目「<b>{{ category || '全部' }}</b>」
        <a @click="clearFilter">清除</a>
      </span>
    </div>

    <!-- 商品表格 -->
    <div class="adm-card table-card">
      <el-table :data="products" stripe v-loading="loading" size="default">
        <el-table-column prop="pid" label="ID" width="64" align="center" />

        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="prod-cell">
              <ProductThumb :image="row.image" :category="row.category" :name="row.name" width="56px" height="56px" radius="8px" />
              <div class="prod-info">
                <div class="prod-name">{{ row.name }}</div>
                <div class="prod-desc">{{ row.description || '暂无描述' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="类目" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="light" :color="catColor(row.category)" style="border:none">{{ row.category }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="价格" width="110" align="center">
          <template #default="{ row }"><span class="price">{{ row.price }}</span></template>
        </el-table-column>

        <el-table-column label="库存" width="90" align="center">
          <template #default="{ row }">
            <span :class="['stock', row.stock <= 0 ? 'out' : row.stock < 50 ? 'low' : '']">
              {{ row.stock }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="评分" width="80" align="center">
          <template #default="{ row }">
            <span class="rating">⭐ {{ row.rating ?? '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialog.visible" :title="dialog.isEdit ? '编辑商品' : '新增商品'"
      width="680px" destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="prod-form">
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="如：运动跑鞋 Air Max Pro" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="价格 ¥" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :step="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="类目" prop="category">
              <el-select v-model="form.category" filterable allow-create style="width:100%" placeholder="选择或输入">
                <el-option v-for="c in cats" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评分（0-5）">
              <el-input-number v-model="form.rating" :min="0" :max="5" :precision="1" :step="0.1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评论数">
              <el-input-number v-model="form.reviewCount" :min="0" :step="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 图片预览 + 输入 -->
        <el-form-item label="商品图片">
          <div class="img-row">
            <div class="img-preview">
              <ProductThumb :image="form.image" :category="form.category" :name="form.name" width="72px" height="72px" radius="8px" />
            </div>
            <div class="img-input">
              <el-input v-model="form.image" placeholder="/images/xxx.jpg 或完整 URL" />
              <small>留空将自动显示按类目的占位图；放图到 <code>yuexuan-app/public/images/</code> 后填文件名即可，如 shoe1.jpg</small>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="200" show-word-limit
            placeholder="详细描述商品特点、材质、适用场景等..." />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button size="large" @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" size="large" :loading="dialog.submitting" @click="handleSubmit">
          {{ dialog.isEdit ? '保存修改' : '确认新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import { getProducts, createProduct, updateProduct, deleteProduct } from '@/api/product'
import ProductThumb from '@/components/ProductThumb.vue'

const cats = ['运动鞋', 'T恤', '数码', '食品', '家居']
const products = ref([])
const loading = ref(false)
const keyword = ref('')
const category = ref('')
const formRef = ref(null)

const dialog = reactive({ visible: false, isEdit: false, submitting: false })

const defaultForm = () => ({
  pid: null, name: '', price: 0, category: '', description: '',
  image: '', stock: 0, rating: 0, reviewCount: 0
})
const form = reactive(defaultForm())

const rules = {
  name:     [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price:    [{ required: true, message: '请输入价格', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类目', trigger: 'change' }],
}

const CAT_COLOR = {
  '运动鞋': '#E65100', 'T恤': '#7B1FA2', '数码': '#00838F',
  '食品': '#558B2F', '家居': '#5D4037',
}
function catColor(c) { return CAT_COLOR[c] || '#666' }

async function load() {
  loading.value = true
  try {
    const params = {}
    if (keyword.value) params.keyword = keyword.value
    if (category.value) params.category = category.value
    products.value = await getProducts(params) || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function clearFilter() {
  keyword.value = ''; category.value = ''; load()
}

function openAdd() {
  dialog.isEdit = false
  Object.assign(form, defaultForm())
  dialog.visible = true
}

function openEdit(row) {
  dialog.isEdit = true
  Object.assign(form, { ...row })
  dialog.visible = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  dialog.submitting = true
  try {
    const { pid, ...data } = form
    if (dialog.isEdit) {
      await updateProduct(pid, data); ElMessage.success('更新成功')
    } else {
      await createProduct(data); ElMessage.success('新增成功')
    }
    dialog.visible = false
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally { dialog.submitting = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」吗？此操作不可恢复。`, '确认删除', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消'
    })
    await deleteProduct(row.pid)
    ElMessage.success('删除成功')
    load()
  } catch (e) { /* 取消或失败 */ }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 18px;
  padding: 20px 24px;
  flex-wrap: wrap;
}
.spacer { flex: 1; }
.count-tip {
  font-size: var(--fs-sm); color: var(--text-muted);
}
.count-tip b { color: var(--primary); margin: 0 2px; }
.count-tip a { color: var(--primary); cursor: pointer; margin-left: 6px; padding-bottom: 2px; border-bottom: 1px solid var(--primary); }

@media (max-width: 640px) {
  .toolbar { padding: 16px 16px; gap: 8px; }
  .toolbar :deep(.el-input), .toolbar :deep(.el-select) { width: 100% !important; }
  .toolbar :deep(.el-button) { width: 100%; }
  .spacer { display: none; }
  .count-tip { width: 100%; text-align: center; }
}

.table-card { padding: 10px 18px 18px; }

/* 商品单元格 */
.prod-cell { display: flex; align-items: center; gap: 14px; }
.prod-info { min-width: 0; }
.prod-name {
  font-size: var(--fs-body); font-weight: 500; color: var(--text-primary); line-height: 1.4;
}
.prod-desc {
  font-size: var(--fs-xs); color: var(--text-muted); margin-top: 4px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 320px;
}

.stock { font-weight: 600; color: var(--text-primary); }
.stock.low { color: #E67E22; }
.stock.out { color: var(--text-muted); }
.rating { font-size: var(--fs-xs); color: var(--text-secondary); }

/* 表单图片预览行 */
.img-row { display: flex; gap: 14px; width: 100%; align-items: flex-start; }
.img-preview { flex-shrink: 0; border: 1px solid var(--border); border-radius: var(--r-md); overflow: hidden; box-shadow: var(--shadow-xs); }
.img-input { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.img-input small { font-size: var(--fs-xs); color: var(--text-muted); line-height: 1.6; }
.img-input code { background: var(--bg-soft); padding: 1px 6px; border-radius: var(--r-xs); color: var(--primary); font-family: monospace; }
</style>