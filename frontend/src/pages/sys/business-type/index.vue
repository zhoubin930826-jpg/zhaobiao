<template>
  <div>
    <div class="i-layout-page-header">
      <PageHeader title="类型管理" hidden-breadcrumb />
    </div>
    <Card :bordered="false" dis-hover class="ivu-mt" ref="card">
      <div class="ivu-inline-block ivu-fr">
        <Tooltip class="ivu-ml" content="刷新" placement="top">
          <i-link @click.native="getData"><Icon custom="i-icon i-icon-refresh" /></i-link>
        </Tooltip>
      </div>

      <div class="ivu-mt-16 ivu-mb-8">
        <Button type="primary" @click="handleAdd">
          <Icon type="md-add" /> 新增类型
        </Button>
      </div>

      <Table :columns="columns" :data="list" :loading="loading" class="ivu-mt">
        <template slot="enabled" slot-scope="{ row }">
          <Badge v-if="row.enabled" color="green" text="启用" />
          <Badge v-else color="red" text="禁用" />
        </template>
        <template slot="action" slot-scope="{ row }">
          <div @click.stop.prevent>
            <a @click="handleEdit(row)">编辑</a>
            <Divider type="vertical" />
            <a @click="handleToggleStatus(row)">{{ row.enabled ? '禁用' : '启用' }}</a>
            <Divider type="vertical" />
            <a style="color: #ed4014" @click="handleDelete(row)">删除</a>
          </div>
        </template>
      </Table>

      <Modal
        v-model="modal.show"
        :title="modal.type === 'add' ? '新增类型' : '编辑类型'"
        width="560"
        :transfer="false"
        :before-close="handleCloseModal"
      >
        <Form ref="typeForm" :model="formData" :rules="formRules" label-position="top">
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="类型编码" prop="code">
                <Input v-model="formData.code" placeholder="如 ENGINEERING" :disabled="modal.type === 'edit'" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="类型名称" prop="name">
                <Input v-model="formData.name" placeholder="请输入类型名称" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="排序">
                <InputNumber v-model="formData.sortOrder" :min="0" :precision="0" style="width: 100%;" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="状态" prop="enabled">
                <RadioGroup v-model="formData.enabled">
                  <Radio :label="true">启用</Radio>
                  <Radio :label="false">禁用</Radio>
                </RadioGroup>
              </FormItem>
            </Col>
          </Row>
          <FormItem label="描述">
            <Input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述（选填）" />
          </FormItem>
        </Form>
        <template #footer>
          <Button @click="handleCloseModal">取消</Button>
          <Button type="primary" :loading="submitting" @click="handleSubmit">保存</Button>
        </template>
      </Modal>
    </Card>
  </div>
</template>

<script>
    import {
        listBusinessTypes,
        createBusinessType,
        updateBusinessType,
        updateBusinessTypeStatus,
        deleteBusinessType
    } from '@api/system';

    export default {
        name: 'system-business-type',
        data () {
            return {
                loading: false,
                submitting: false,
                list: [],
                columns: [
                    { title: '编码', key: 'code', minWidth: 140 },
                    { title: '名称', key: 'name', minWidth: 140 },
                    { title: '排序', key: 'sortOrder', minWidth: 80 },
                    { title: '状态', slot: 'enabled', minWidth: 90 },
                    { title: '描述', key: 'description', minWidth: 220 },
                    { title: '操作', slot: 'action', minWidth: 220, align: 'center', fixed: 'right' }
                ],
                modal: {
                    show: false,
                    type: 'add'
                },
                formData: {
                    id: null,
                    code: '',
                    name: '',
                    enabled: true,
                    sortOrder: 0,
                    description: ''
                },
                formRules: {
                    code: [
                        { required: true, message: '请输入类型编码', trigger: 'blur' },
                        { max: 64, message: '编码最多64位', trigger: 'blur' }
                    ],
                    name: [
                        { required: true, message: '请输入类型名称', trigger: 'blur' },
                        { max: 64, message: '名称最多64位', trigger: 'blur' }
                    ],
                    enabled: [{ required: true, type: 'boolean', message: '请选择状态', trigger: 'change' }]
                }
            };
        },
        mounted () {
            this.getData();
        },
        methods: {
            getData () {
                if (this.loading) return;
                this.loading = true;
                listBusinessTypes().then(res => {
                    this.list = Array.isArray(res) ? res : [];
                    this.loading = false;
                }).catch(() => {
                    this.loading = false;
                });
            },
            handleAdd () {
                this.modal = { show: true, type: 'add' };
                this.formData = { id: null, code: '', name: '', enabled: true, sortOrder: 0, description: '' };
                this.$nextTick(() => this.$refs.typeForm && this.$refs.typeForm.resetFields());
            },
            handleEdit (row) {
                this.modal = { show: true, type: 'edit' };
                this.formData = {
                    id: row.id,
                    code: row.code || '',
                    name: row.name || '',
                    enabled: !!row.enabled,
                    sortOrder: row.sortOrder != null ? row.sortOrder : 0,
                    description: row.description || ''
                };
                this.$nextTick(() => this.$refs.typeForm && this.$refs.typeForm.clearValidate());
            },
            handleCloseModal () {
                this.modal.show = false;
            },
            handleSubmit () {
                this.$refs.typeForm.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    const payload = {
                        code: this.formData.code,
                        name: this.formData.name,
                        enabled: this.formData.enabled,
                        sortOrder: this.formData.sortOrder,
                        description: this.formData.description
                    };
                    const req = this.modal.type === 'add'
                        ? createBusinessType(payload)
                        : updateBusinessType(this.formData.id, payload);
                    req.then(() => {
                        this.submitting = false;
                        this.modal.show = false;
                        this.$Message.success(this.modal.type === 'add' ? '新增类型成功' : '更新类型成功');
                        this.getData();
                    }).catch(() => {
                        this.submitting = false;
                    });
                });
            },
            handleToggleStatus (row) {
                updateBusinessTypeStatus(row.id, { enabled: !row.enabled }).then(() => {
                    this.$Message.success('状态更新成功');
                    this.getData();
                });
            },
            handleDelete (row) {
                this.$Modal.confirm({
                    title: '确认删除',
                    content: `确定删除类型「${row.name}」吗？`,
                    onOk: () => deleteBusinessType(row.id).then(() => {
                        this.$Message.success('删除成功');
                        this.getData();
                    })
                });
            }
        }
    };
</script>
