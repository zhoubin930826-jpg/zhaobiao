<template>
  <div>
    <div class="page-header">
      <div>
        <h2>菜单管理</h2>
        <p>菜单用于路由展示，按钮类型菜单需要绑定权限编码。</p>
      </div>
      <div class="toolbar">
        <el-button v-if="canEdit" type="primary" @click="openDialog()">新增菜单</el-button>
        <el-button plain @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-card class="page-card">
      <el-table :data="menus" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="code" label="编码" min-width="160" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="routePath" label="路由" min-width="140" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="150" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="显示" width="90">
          <template #default="{ row }">
            <el-tag :type="row.visible ? 'success' : 'info'">{{ row.visible ? "显示" : "隐藏" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">{{ row.enabled ? "启用" : "停用" }}</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑菜单' : '新增菜单'" width="720px">
      <el-form :model="dialog.form" label-width="110px">
        <el-form-item label="菜单编码"><el-input v-model="dialog.form.code" /></el-form-item>
        <el-form-item label="菜单名称"><el-input v-model="dialog.form.name" /></el-form-item>
        <el-form-item label="菜单类型">
          <el-select v-model="dialog.form.type" style="width: 100%">
            <el-option label="目录" value="DIRECTORY" />
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="父级菜单">
          <el-tree-select
            v-model="dialog.form.parentId"
            :data="parentMenuOptions"
            node-key="id"
            check-strictly
            clearable
            :props="{ value: 'id', label: 'name', children: 'children' }"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="前端路由"><el-input v-model="dialog.form.routePath" /></el-form-item>
        <el-form-item label="组件路径"><el-input v-model="dialog.form.component" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="dialog.form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dialog.form.sortOrder" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="权限编码">
          <el-select v-model="dialog.form.permissionCode" clearable filterable style="width: 100%">
            <el-option v-for="permission in permissions" :key="permission.id" :label="permission.code" :value="permission.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否显示"><el-switch v-model="dialog.form.visible" /></el-form-item>
        <el-form-item label="是否启用"><el-switch v-model="dialog.form.enabled" /></el-form-item>
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
const menus = ref([]);
const permissions = ref([]);

const dialog = reactive({
  visible: false,
  form: {
    id: null,
    code: "",
    name: "",
    type: "MENU",
    parentId: null,
    routePath: "",
    component: "",
    icon: "",
    sortOrder: 10,
    visible: true,
    enabled: true,
    permissionCode: "",
    description: ""
  }
});

const canEdit = computed(() => authStore.hasPermission("menu:edit"));
const parentMenuOptions = computed(() => menus.value);

const loadData = async () => {
  const [menuTree, permissionList] = await Promise.all([
    request.get("/api/admin/menus"),
    request.get("/api/admin/permissions")
  ]);
  menus.value = menuTree;
  permissions.value = permissionList;
};

const openDialog = (row = null) => {
  dialog.visible = true;
  dialog.form = row
    ? {
        id: row.id,
        code: row.code,
        name: row.name,
        type: row.type,
        parentId: row.parentId,
        routePath: row.routePath,
        component: row.component,
        icon: row.icon,
        sortOrder: row.sortOrder,
        visible: row.visible,
        enabled: row.enabled,
        permissionCode: row.permissionCode,
        description: row.description
      }
    : {
        id: null,
        code: "",
        name: "",
        type: "MENU",
        parentId: null,
        routePath: "",
        component: "",
        icon: "",
        sortOrder: 10,
        visible: true,
        enabled: true,
        permissionCode: "",
        description: ""
      };
};

const submitForm = async () => {
  const payload = { ...dialog.form };
  if (dialog.form.id) {
    await request.put(`/api/admin/menus/${dialog.form.id}`, payload);
    ElMessage.success("菜单已更新");
  } else {
    await request.post("/api/admin/menus", payload);
    ElMessage.success("菜单已新增");
  }
  dialog.visible = false;
  await loadData();
};

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除菜单 ${row.name} 吗？`, "提示", { type: "warning" });
  await request.delete(`/api/admin/menus/${row.id}`);
  ElMessage.success("菜单已删除");
  await loadData();
};

onMounted(loadData);
</script>

