<template>
  <div>
    <div class="page-header">
      <div>
        <h2>权限管理</h2>
        <p>接口访问和按钮显示都基于权限编码控制。</p>
      </div>
      <div class="toolbar">
        <el-button v-if="canEdit" type="primary" @click="openDialog()">新增权限</el-button>
        <el-button plain @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-card class="page-card">
      <el-table :data="permissions" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="权限编码" min-width="180" />
        <el-table-column prop="name" label="权限名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="220" />
        <el-table-column v-if="canEdit" label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑权限' : '新增权限'" width="520px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="权限编码"><el-input v-model="dialog.form.code" /></el-form-item>
        <el-form-item label="权限名称"><el-input v-model="dialog.form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="dialog.form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();
const permissions = ref([]);

const dialog = reactive({
  visible: false,
  form: {
    id: null,
    code: "",
    name: "",
    description: ""
  }
});

const canEdit = computed(() => authStore.hasPermission("permission:edit"));

const loadData = async () => {
  permissions.value = await request.get("/api/admin/permissions");
};

const openDialog = (row = null) => {
  dialog.visible = true;
  dialog.form = row
    ? { id: row.id, code: row.code, name: row.name, description: row.description }
    : { id: null, code: "", name: "", description: "" };
};

const submitForm = async () => {
  const payload = {
    code: dialog.form.code,
    name: dialog.form.name,
    description: dialog.form.description
  };
  if (dialog.form.id) {
    await request.put(`/api/admin/permissions/${dialog.form.id}`, payload);
    ElMessage.success("权限已更新");
  } else {
    await request.post("/api/admin/permissions", payload);
    ElMessage.success("权限已新增");
  }
  dialog.visible = false;
  await loadData();
};

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除权限 ${row.code} 吗？`, "提示", { type: "warning" });
  await request.delete(`/api/admin/permissions/${row.id}`);
  ElMessage.success("权限已删除");
  await loadData();
};

onMounted(loadData);
</script>
