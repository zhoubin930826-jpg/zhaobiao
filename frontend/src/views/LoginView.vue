<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-intro">
        <h1>招标系统后台</h1>
        <p>支持多角色、菜单管理、按钮权限、审核记录和操作日志。</p>
        <div class="login-tips">
          <div>初始管理员账号: <strong>admin</strong></div>
          <div>初始管理员密码: <strong>adminqwert</strong></div>
          <el-link type="primary" href="/swagger-ui.html" target="_blank">打开 Swagger</el-link>
        </div>
      </div>

      <el-card shadow="never" class="login-form-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" label-position="top" @submit.prevent="handleLogin">
              <el-form-item label="用户名">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="密码">
                <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
              </el-form-item>
              <el-button type="primary" class="full-width" @click="handleLogin">登录</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" label-position="top">
              <el-form-item label="用户名"><el-input v-model="registerForm.username" /></el-form-item>
              <el-form-item label="手机号"><el-input v-model="registerForm.phone" /></el-form-item>
              <el-form-item label="邮箱"><el-input v-model="registerForm.email" /></el-form-item>
              <el-form-item label="公司名称"><el-input v-model="registerForm.companyName" /></el-form-item>
              <el-form-item label="联系人"><el-input v-model="registerForm.contactPerson" /></el-form-item>
              <el-form-item label="统一社会信用代码"><el-input v-model="registerForm.unifiedSocialCreditCode" /></el-form-item>
              <el-form-item label="真实姓名"><el-input v-model="registerForm.realName" /></el-form-item>
              <el-form-item label="密码"><el-input v-model="registerForm.password" type="password" show-password /></el-form-item>
              <el-form-item label="确认密码"><el-input v-model="registerForm.confirmPassword" type="password" show-password /></el-form-item>
              <el-button type="primary" class="full-width" @click="handleRegister">提交注册</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref("login");

const loginForm = reactive({
  username: "admin",
  password: "adminqwert"
});

const registerForm = reactive({
  username: "",
  phone: "",
  email: "",
  companyName: "",
  contactPerson: "",
  unifiedSocialCreditCode: "",
  realName: "",
  password: "",
  confirmPassword: ""
});

const handleLogin = async () => {
  await authStore.login(loginForm);
  ElMessage.success("登录成功");
  router.push("/dashboard");
};

const handleRegister = async () => {
  await request.post("/api/auth/register", registerForm);
  ElMessage.success("注册成功，请等待管理员审核");
  Object.keys(registerForm).forEach((key) => {
    registerForm[key] = "";
  });
  activeTab.value = "login";
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #eff6ff, #eef2ff 50%, #f8fafc);
}

.login-card {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
  align-items: center;
}

.login-intro h1 {
  margin: 0;
  font-size: 42px;
}

.login-intro p {
  margin: 16px 0 24px;
  font-size: 18px;
  color: #4b5563;
  line-height: 1.7;
}

.login-tips {
  display: grid;
  gap: 10px;
  color: #1f2937;
}

.login-form-card {
  border: none;
  border-radius: 20px;
}

.full-width {
  width: 100%;
}

@media (max-width: 900px) {
  .login-card {
    grid-template-columns: 1fr;
  }
}
</style>

