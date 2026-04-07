<template>
  <div>
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>支持注册用户审核、驳回原因记录和多角色分配。</p>
      </div>
      <div class="toolbar">
        <el-button type="primary" plain @click="loadData">刷新</el-button>
      </div>
    </div>

    <el-card class="page-card">
      <el-table :data="users" row-key="id" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="110" />
        <el-table-column prop="companyName" label="公司名称" min-width="180" />
        <el-table-column prop="contactPerson" label="联系人" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status] || 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="role in row.roleNames" :key="role" class="tag-space">{{ role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditReason" label="审核原因" min-width="160" />
        <el-table-column label="操作" min-width="280" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button
                v-if="canAudit"
                size="small"
                type="success"
                @click="openAuditDialog(row, true)"
              >
                通过
              </el-button>
              <el-button
                v-if="canAudit"
                size="small"
                type="warning"
                @click="openAuditDialog(row, false)"
              >
                驳回
              </el-button>
              <el-button
                v-if="canRoleUpdate"
                size="small"
                type="primary"
                plain
                @click="openRoleDialog(row)"
              >
                分配角色
              </el-button>
              <el-button
                v-if="canAuditRecord"
                size="small"
                plain
                @click="goAuditRecords(row.id)"
              >
                审核记录
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="auditDialog.visible" :title="auditDialog.approved ? '审核通过' : '审核驳回'" width="420px">
      <el-form label-position="top">
        <el-form-item label="用户">
          <el-input :model-value="auditDialog.row?.username" disabled />
        </el-form-item>
        <el-form-item v-if="!auditDialog.approved" label="驳回原因">
          <el-input v-model="auditDialog.reason" type="textarea" :rows="4" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialog.visible" title="分配角色" width="520px">
      <el-form label-position="top">
        <el-form-item label="用户">
          <el-input :model-value="roleDialog.row?.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="roleDialog.roleIds" multiple filterable style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import request from "@/utils/request";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();

const users = ref([]);
const roles = ref([]);

const auditDialog = reactive({
  visible: false,
  approved: true,
  row: null,
  reason: ""
});

const roleDialog = reactive({
  visible: false,
  row: null,
  roleIds: []
});

const statusTypeMap = {
  APPROVED: "success",
  PENDING: "info",
  REJECTED: "danger",
  DISABLED: "warning"
};

const canAudit = computed(() => authStore.hasPermission("user:audit"));
const canRoleUpdate = computed(() => authStore.hasPermission("user:role:update"));
const canAuditRecord = computed(() => authStore.hasPermission("user:audit:record:view"));

const loadData = async () => {
  const [userList, roleList] = await Promise.all([
    request.get("/api/admin/users"),
    request.get("/api/admin/roles")
  ]);
  users.value = userList;
  roles.value = roleList;
};

const openAuditDialog = (row, approved) => {
  auditDialog.visible = true;
  auditDialog.approved = approved;
  auditDialog.row = row;
  auditDialog.reason = "";
};

const submitAudit = async () => {
  await request.put(`/api/admin/users/${auditDialog.row.id}/audit`, {
    approved: auditDialog.approved,
    reason: auditDialog.reason
  });
  ElMessage.success("审核操作已完成");
  auditDialog.visible = false;
  await loadData();
};

const openRoleDialog = (row) => {
  roleDialog.visible = true;
  roleDialog.row = row;
  roleDialog.roleIds = [...row.roleIds];
};

const submitRoles = async () => {
  await request.put(`/api/admin/users/${roleDialog.row.id}/roles`, {
    roleIds: roleDialog.roleIds
  });
  ElMessage.success("角色分配已更新");
  roleDialog.visible = false;
  await loadData();
};

const goAuditRecords = (userId) => {
  router.push({ path: "/audit-records", query: { userId } });
};

onMounted(loadData);
</script>

<style scoped>
.tag-space {
  margin-right: 6px;
  margin-bottom: 6px;
}
</style>

