import { defineStore } from "pinia";
import request from "@/utils/request";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("zb_token") || "",
    user: null
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    permissions: (state) => state.user?.permissions || [],
    menuTree: (state) => state.user?.menus || [],
    roleNames: (state) => state.user?.roleNames || []
  },
  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem("zb_token", token);
    },
    clearAuth() {
      this.token = "";
      this.user = null;
      localStorage.removeItem("zb_token");
    },
    async login(payload) {
      const data = await request.post("/api/auth/login", payload);
      this.setToken(data.token);
      this.user = data.user;
      return data;
    },
    async fetchCurrentUser() {
      if (!this.token) {
        return null;
      }
      this.user = await request.get("/api/auth/me");
      return this.user;
    },
    logout() {
      this.clearAuth();
    },
    hasPermission(code) {
      return this.permissions.includes(code);
    }
  }
});

