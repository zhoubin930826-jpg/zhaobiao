<template>
  <div>
    <div class="page-header">
      <div>
        <h2>工作台</h2>
        <p>当前系统已切换为多角色 + 菜单 + 按钮权限模型。</p>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="8" v-for="card in cards" :key="card.label">
        <el-card class="page-card info-card">
          <div class="info-label">{{ card.label }}</div>
          <div class="info-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="page-card section-card">
      <template #header>当前账号信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ authStore.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ authStore.user?.realName || "-" }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ authStore.user?.companyName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ authStore.user?.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ authStore.user?.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ authStore.user?.email }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ authStore.user?.unifiedSocialCreditCode }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ authStore.roleNames.join(" / ") }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="page-card section-card">
      <template #header>当前权限</template>
      <div class="permission-wrap">
        <el-tag v-for="permission in authStore.permissions" :key="permission" class="permission-tag">
          {{ permission }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();

const cards = computed(() => [
  { label: "角色数量", value: authStore.user?.roleCodes?.length || 0 },
  { label: "权限数量", value: authStore.permissions.length },
  { label: "菜单节点", value: authStore.menuTree.length }
]);
</script>

<style scoped>
.info-card {
  margin-bottom: 16px;
}

.info-label {
  color: #6b7280;
  margin-bottom: 10px;
}

.info-value {
  font-size: 28px;
  font-weight: 700;
}

.section-card {
  margin-top: 20px;
}

.permission-wrap {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.permission-tag {
  margin-bottom: 6px;
}
</style>

