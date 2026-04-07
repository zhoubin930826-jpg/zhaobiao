<template>
  <div>
    <div class="page-header">
      <div>
        <h2>操作日志</h2>
        <p>记录关键管理动作的执行结果，方便审计和问题排查。</p>
      </div>
      <div class="toolbar">
        <el-button plain @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-card class="page-card">
      <el-table :data="logs" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="module" label="模块" min-width="120" />
        <el-table-column prop="action" label="动作" min-width="140" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'">{{ row.success ? "成功" : "失败" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorUsername" label="操作人" min-width="120" />
        <el-table-column prop="requestMethod" label="方法" width="90" />
        <el-table-column prop="requestUri" label="请求地址" min-width="220" />
        <el-table-column prop="ipAddress" label="IP" min-width="130" />
        <el-table-column prop="detail" label="详情" min-width="180" />
        <el-table-column prop="createdAt" label="时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import request from "@/utils/request";

const logs = ref([]);

const loadData = async () => {
  logs.value = await request.get("/api/admin/operation-logs");
};

onMounted(loadData);
</script>
