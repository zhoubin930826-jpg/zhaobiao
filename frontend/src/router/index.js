import { createRouter, createWebHashHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import MainLayout from "@/layouts/MainLayout.vue";
import LoginView from "@/views/LoginView.vue";
import DashboardView from "@/views/DashboardView.vue";
import ProfileView from "@/views/ProfileView.vue";
import UsersView from "@/views/UsersView.vue";
import RolesView from "@/views/RolesView.vue";
import PermissionsView from "@/views/PermissionsView.vue";
import MenusView from "@/views/MenusView.vue";
import AuditRecordsView from "@/views/AuditRecordsView.vue";
import OperationLogsView from "@/views/OperationLogsView.vue";

const routes = [
  {
    path: "/login",
    name: "login",
    component: LoginView,
    meta: { public: true }
  },
  {
    path: "/",
    component: MainLayout,
    children: [
      {
        path: "",
        redirect: "/dashboard"
      },
      {
        path: "/dashboard",
        name: "dashboard",
        component: DashboardView
      },
      {
        path: "/profile",
        name: "profile",
        component: ProfileView
      },
      {
        path: "/users",
        name: "users",
        component: UsersView,
        meta: { permission: "user:view" }
      },
      {
        path: "/roles",
        name: "roles",
        component: RolesView,
        meta: { permission: "role:view" }
      },
      {
        path: "/permissions",
        name: "permissions",
        component: PermissionsView,
        meta: { permission: "permission:view" }
      },
      {
        path: "/menus",
        name: "menus",
        component: MenusView,
        meta: { permission: "menu:view" }
      },
      {
        path: "/audit-records",
        name: "audit-records",
        component: AuditRecordsView,
        meta: { permission: "user:audit:record:view" }
      },
      {
        path: "/operation-logs",
        name: "operation-logs",
        component: OperationLogsView,
        meta: { permission: "operation:log:view" }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  if (to.meta.public) {
    if (authStore.isLoggedIn) {
      return "/dashboard";
    }
    return true;
  }

  if (!authStore.isLoggedIn) {
    return "/login";
  }

  if (!authStore.user) {
    try {
      await authStore.fetchCurrentUser();
    } catch (error) {
      authStore.clearAuth();
      return "/login";
    }
  }

  if (to.meta.permission && !authStore.hasPermission(to.meta.permission)) {
    return "/dashboard";
  }
  return true;
});

export default router;
