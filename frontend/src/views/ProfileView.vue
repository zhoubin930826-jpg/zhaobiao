<template>
  <div>
    <div class="page-header">
      <div>
        <h2>个人中心</h2>
        <p>可以维护基础联系人信息和企业主体信息。</p>
      </div>
    </div>

    <el-card class="page-card form-card">
      <el-form :model="form" label-width="140px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="公司名称"><el-input v-model="form.companyName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="统一社会信用代码"><el-input v-model="form.unifiedSocialCreditCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="新密码"><el-input v-model="form.password" type="password" show-password placeholder="不修改可留空" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="确认新密码"><el-input v-model="form.confirmPassword" type="password" show-password placeholder="不修改可留空" /></el-form-item></el-col>
        </el-row>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();

const form = reactive({
  username: "",
  phone: "",
  email: "",
  realName: "",
  companyName: "",
  contactPerson: "",
  unifiedSocialCreditCode: "",
  password: "",
  confirmPassword: ""
});

watch(
  () => authStore.user,
  (user) => {
    if (!user) {
      return;
    }
    form.username = user.username || "";
    form.phone = user.phone || "";
    form.email = user.email || "";
    form.realName = user.realName || "";
    form.companyName = user.companyName || "";
    form.contactPerson = user.contactPerson || "";
    form.unifiedSocialCreditCode = user.unifiedSocialCreditCode || "";
    form.password = "";
    form.confirmPassword = "";
  },
  { immediate: true }
);

const handleSave = async () => {
  await request.put("/api/profile", {
    phone: form.phone,
    email: form.email,
    realName: form.realName,
    companyName: form.companyName,
    contactPerson: form.contactPerson,
    unifiedSocialCreditCode: form.unifiedSocialCreditCode,
    password: form.password,
    confirmPassword: form.confirmPassword
  });
  await authStore.fetchCurrentUser();
  ElMessage.success("个人信息已更新");
  form.password = "";
  form.confirmPassword = "";
};
</script>

<style scoped>
.form-card {
  padding: 10px 6px;
}
</style>
