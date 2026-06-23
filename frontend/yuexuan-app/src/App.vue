<template>
  <div class="app-shell">
    <SiteHeader />
    <main class="app-main">
      <router-view />
    </main>
    <SiteFooter />
  </div>
</template>

<script setup lang="ts">
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'
</script>

<style>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
}
.app-main {
  flex: 1;
}
</style>