<template>
  <div>
    <div class="page-header">
      <div>
        <h2>角色管理</h2>
        <p>角色绑定菜单和权限，按钮菜单必须和权限编码保持一致。</p>
      </div>
      <div class="toolbar">
        <el-button v-if="canEdit" type="primary" @click="openDialog()">新增角色</el-button>
        <el-button plain @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-card class="page-card">
      <el-table :data="roles" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="编码" min-width="140" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column label="权限" min-width="260">
          <template #default="{ row }">
            <el-tag v-for="item in row.permissionCodes" :key="item" class="tag-space">{{ item }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="菜单" min-width="240">
          <template #default="{ row }">
            <el-tag v-for="item in row.menuCodes" :key="item" type="success" class="tag-space">{{ item }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内置" width="90">
          <template #default="{ row }">
            <el-tag :type="row.builtIn ? 'warning' : 'info'">{{ row.builtIn ? "是" : "否" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canEdit" label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" plain @click="openDialog(row)">编辑</el-button>
              <el-button v-if="!row.builtIn" size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑角色' : '新增角色'" width="780px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="角色编码"><el-input v-model="dialog.form.code" :disabled="dialog.form.builtIn" /></el-form-item>
        <el-form-item label="角色名称"><el-input v-model="dialog.form.name" /></el-form-item>
        <el-form-item label="角色描述"><el-input v-model="dialog.form.description" /></el-form-item>
        <el-form-item label="权限分配">
          <el-checkbox-group v-model="dialog.form.permissionIds">
            <el-checkbox v-for="permission in permissions" :key="permission.id" :label="permission.id">
              {{ permission.code }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="菜单分配">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            show-checkbox
            node-key="id"
            default-expand-all
            :props="{ label: 'name', children: 'children' }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import request from "@/utils/request";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();
const roles = ref([]);
const permissions = ref([]);
const menuTree = ref([]);
const menuTreeRef = ref();

const dialog = reactive({
  visible: false,
  form: {
    id: null,
    builtIn: false,
    code: "",
    name: "",
    description: "",
    permissionIds: [],
    menuIds: []
  }
});

const canEdit = computed(() => authStore.hasPermission("role:edit"));

const loadData = async () => {
  const [roleList, permissionList, menuList] = await Promise.all([
    request.get("/api/admin/roles"),
    request.get("/api/admin/permissions"),
    request.get("/api/admin/menus")
  ]);
  roles.value = roleList;
  permissions.value = permissionList;
  menuTree.value = menuList;
};

const resetForm = () => {
  dialog.form = {
    id: null,
    builtIn: false,
    code: "",
    name: "",
    description: "",
    permissionIds: [],
    menuIds: []
  };
};

const openDialog = async (row = null) => {
  resetForm();
  dialog.visible = true;
  await nextTick();
  menuTreeRef.value?.setCheckedKeys([]);
  if (!row) {
    return;
  }
  dialog.form = {
    id: row.id,
    builtIn: row.builtIn,
    code: row.code,
    name: row.name,
    description: row.description,
    permissionIds: [...row.permissionIds],
    menuIds: [...row.menuIds]
  };
  await nextTick();
  menuTreeRef.value?.setCheckedKeys(row.menuIds || []);
};

const submitForm = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys(false) || [];
  const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || [];
  const menuIds = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]));
  const payload = {
    code: dialog.form.code,
    name: dialog.form.name,
    description: dialog.form.description,
    permissionIds: dialog.form.permissionIds,
    menuIds
  };
  if (dialog.form.id) {
    await request.put(`/api/admin/roles/${dialog.form.id}`, payload);
    ElMessage.success("角色已更新");
  } else {
    await request.post("/api/admin/roles", payload);
    ElMessage.success("角色已新增");
  }
  dialog.visible = false;
  await loadData();
};

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除角色 ${row.name} 吗？`, "提示", { type: "warning" });
  await request.delete(`/api/admin/roles/${row.id}`);
  ElMessage.success("角色已删除");
  await loadData();
};

onMounted(loadData);
</script>

<style scoped>
.tag-space {
  margin-right: 6px;
  margin-bottom: 6px;
}
</style>
