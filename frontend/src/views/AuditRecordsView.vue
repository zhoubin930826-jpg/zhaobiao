<template>
  <div>
    <div class="page-header">
      <div>
        <h2>审核记录</h2>
        <p>查看注册用户每次审核的结果与原因。</p>
      </div>
      <div class="toolbar">
        <el-select v-model="selectedUserId" filterable placeholder="请选择用户" style="width: 320px" @change="loadRecords">
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="`${user.username} / ${user.companyName}`"
            :value="user.id"
          />
        </el-select>
      </div>
    </div>

    <el-card class="page-card">
      <el-table :data="records" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="decision" label="审核结果" width="120">
          <template #default="{ row }">
            <el-tag :type="row.decision === 'APPROVED' ? 'success' : 'danger'">{{ row.decision }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="220" />
        <el-table-column prop="auditorUsername" label="审核人" min-width="120" />
        <el-table-column prop="createdAt" label="审核时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request";

const route = useRoute();
const users = ref([]);
const records = ref([]);
const selectedUserId = ref(route.query.userId ? Number(route.query.userId) : null);

const loadUsers = async () => {
  users.value = await request.get("/api/admin/users");
  if (!selectedUserId.value && users.value.length) {
    selectedUserId.value = users.value[0].id;
  }
};

const loadRecords = async () => {
  if (!selectedUserId.value) {
    records.value = [];
    return;
  }
  records.value = await request.get(`/api/admin/users/${selectedUserId.value}/audit-records`);
};

onMounted(async () => {
  await loadUsers();
  await loadRecords();
});
</script>

