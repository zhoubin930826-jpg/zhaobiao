<template>
  <div class="layout-shell">
    <aside class="layout-aside">
      <div class="brand">
        <div class="brand-title">招标系统后台</div>
        <div class="brand-subtitle">Vue3 + Element Plus</div>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          class="side-menu"
          router
        >
          <app-menu-node
            v-for="menu in sidebarMenus"
            :key="menu.id"
            :menu="menu"
          />
        </el-menu>
      </el-scrollbar>
    </aside>

    <div class="layout-main">
      <header class="layout-header">
        <div>
          <div class="header-title">{{ currentTitle }}</div>
          <div class="header-subtitle">{{ authStore.user?.companyName || "欢迎使用招标系统后台" }}</div>
        </div>
        <div class="header-actions">
          <el-tag type="primary">{{ authStore.user?.username }}</el-tag>
          <el-tag>{{ authStore.roleNames.join(" / ") }}</el-tag>
          <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="layout-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import AppMenuNode from "@/components/AppMenuNode.vue";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const sidebarMenus = computed(() =>
  (authStore.menuTree || []).filter((item) => item.enabled && item.visible && item.type !== "BUTTON")
);

const currentTitle = computed(() => {
  const matched = route.matched[route.matched.length - 1];
  return matched?.name || "工作台";
});

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("确认退出当前账号吗？", "提示", {
      type: "warning"
    });
    authStore.logout();
    router.push("/login");
  } catch (error) {
    // ignore cancel
  }
};
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
}

.layout-aside {
  background: #111827;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.brand {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.65);
  font-size: 13px;
}

.side-menu {
  border-right: none;
  background: transparent;
}

:deep(.side-menu .el-menu) {
  background: transparent;
}

:deep(.side-menu .el-menu-item),
:deep(.side-menu .el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.82);
}

:deep(.side-menu .el-menu-item.is-active) {
  background: rgba(37, 99, 235, 0.2);
  color: #fff;
}

.layout-main {
  display: flex;
  flex-direction: column;
}

.layout-header {
  height: 72px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
}

.header-title {
  font-size: 22px;
  font-weight: 600;
}

.header-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.layout-content {
  padding: 24px;
}

@media (max-width: 960px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .layout-aside {
    display: none;
  }
}
</style>
