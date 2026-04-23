<template>
  <div>
    <div class="i-layout-page-header">
      <PageHeader title="招标管理" hidden-breadcrumb />
    </div>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <Form :model="query" inline>
        <FormItem>
          <Input v-model="query.keyword" placeholder="标题/项目编号关键词" clearable />
        </FormItem>
        <FormItem>
          <Input v-model="query.region" placeholder="地区" clearable />
        </FormItem>
        <FormItem>
          <Select v-model="query.businessTypeId" clearable placeholder="业务类型" style="width: 180px">
            <Option v-for="item in businessTypeOptions" :key="item.id" :value="item.id">{{ item.name }}</Option>
          </Select>
        </FormItem>
        <FormItem>
          <Button type="primary" @click="handleSearch">查询</Button>
          <Button class="ivu-ml-8" @click="handleResetQuery">重置</Button>
        </FormItem>
      </Form>

      <div class="ivu-mt-8 ivu-mb-8">
        <Button type="primary" @click="handleAdd">
          <Icon type="md-add" /> 新增招标
        </Button>
      </div>

      <Table :columns="columns" :data="list" :loading="loading">
        <template slot="businessType" slot-scope="{ row }">
          {{ row.businessType ? row.businessType.name : '—' }}
        </template>
        <template slot="status" slot-scope="{ row }">
          <Badge v-if="row.status === 'PUBLISHED'" color="green" text="发布中" />
          <Badge v-else-if="row.status === 'CLOSED'" color="default" text="已关闭" />
          <span v-else>{{ row.status }}</span>
        </template>
        <template slot="action" slot-scope="{ row }">
          <div @click.stop.prevent>
            <a @click="handleEdit(row)">编辑</a>
            <Divider type="vertical" />
            <a style="color: #ed4014" @click="handleDelete(row)">删除</a>
          </div>
        </template>
      </Table>

      <div class="ivu-mt ivu-text-center">
        <Page
          :total="total"
          :current.sync="pageNum"
          :page-size="pageSize"
          show-total
          show-sizer
          show-elevator
          @on-change="handleChangePage"
          @on-page-size-change="handleChangePageSize"
        />
      </div>

      <Modal
        v-model="modal.show"
        :title="modal.type === 'add' ? '新增招标' : '编辑招标'"
        width="920"
        :transfer="false"
      >
        <Form ref="tenderForm" :model="formData" :rules="formRules" label-position="top">
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="标题" prop="title">
                <Input v-model="formData.title" maxlength="200" placeholder="请输入招标标题" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="地区" prop="region">
                <Select v-model="formData.region" filterable allow-create placeholder="请选择或输入地区">
                  <Option v-for="item in regionOptions" :key="item" :value="item">{{ item }}</Option>
                </Select>
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="业务类型" prop="businessTypeId">
                <Select
                  v-model="formData.businessTypeId"
                  placeholder="请选择业务类型"
                  @on-change="handleBusinessTypeChange"
                >
                  <Option v-for="item in businessTypeOptions" :key="item.id" :value="item.id">{{ item.name }}</Option>
                </Select>
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="发布时间" prop="publishAt">
                <DatePicker
                  v-model="formData.publishAt"
                  type="datetime"
                  format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择发布时间"
                  style="width: 100%;"
                />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="项目编号" prop="projectCode">
                <Input v-model="formData.projectCode" maxlength="64" placeholder="如 ZB-2026-001" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="招标单位" prop="tenderUnit">
                <Input v-model="formData.tenderUnit" maxlength="128" placeholder="请输入招标单位名称" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="预算" prop="budget">
                <Input v-model="formData.budget" maxlength="64" placeholder="如 580 万元" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="联系人" prop="contactPerson">
                <Input v-model="formData.contactPerson" maxlength="64" placeholder="请输入联系人姓名" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="联系方式" prop="contactPhone">
                <Input v-model="formData.contactPhone" maxlength="32" placeholder="如 0571-88886666" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="截止时间" prop="deadline">
                <DatePicker
                  v-model="formData.deadline"
                  type="datetime"
                  format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择截止时间"
                  style="width: 100%;"
                />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="报名截止时间" prop="signupDeadline">
                <DatePicker
                  v-model="formData.signupDeadline"
                  type="datetime"
                  format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择报名截止时间"
                  style="width: 100%;"
                />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="状态">
                <RadioGroup v-model="formData.status">
                  <Radio label="PUBLISHED">发布中</Radio>
                  <Radio label="CLOSED">已关闭</Radio>
                </RadioGroup>
              </FormItem>
            </Col>
          </Row>
          <FormItem label="正文" prop="content">
            <i-quill-editor
              v-if="modal.show"
              :key="'tender-editor-' + tenderEditorKey"
              v-model="formData.content"
              border
              :min-height="280"
              placeholder="请输入招标正文（富文本），支持标题、列表、引用、链接等"
              @on-change="handleContentEditorChange"
            />
          </FormItem>
          <FormItem label="附件上传">
            <Upload
              multiple
              type="drag"
              :before-upload="handleBeforeUpload"
              :show-upload-list="false"
            >
              <div style="padding: 12px 0">
                <Icon type="ios-cloud-upload" size="28" style="color: #2d8cf0"></Icon>
                <p>点击或拖拽文件到此处上传</p>
              </div>
            </Upload>
            <div class="ivu-mt-8" v-if="attachments.length">
              <div
                v-for="item in attachments"
                :key="item.fileId"
                class="attachment-item"
              >
                <a @click.prevent="previewAttachment(item)">
                  {{ item.fileName || (`文件#${item.fileId}`) }}
                </a>
                <Icon type="md-close" class="attachment-remove" @click="removeAttachment(item.fileId)" />
              </div>
            </div>
          </FormItem>
        </Form>
        <template #footer>
          <Button @click="modal.show = false">取消</Button>
          <Button type="primary" :loading="submitting" @click="handleSubmit">保存</Button>
        </template>
      </Modal>
    </Card>
  </div>
</template>

<script>
    import IQuillEditor from '@/components/quill-editor';
    import {
        listTenders,
        getTenderDetail,
        createTender,
        updateTender,
        deleteTender,
        listBusinessTypeOptions,
        uploadTenderFiles
    } from '@api/system';

    export default {
        name: 'system-tender',
        components: {
            IQuillEditor
        },
        data () {
            const toDate = (value) => {
                if (!value) return null;
                if (value instanceof Date) return value;
                const parsed = new Date(String(value).replace(' ', 'T'));
                return Number.isNaN(parsed.getTime()) ? null : parsed;
            };
            const validatePublishAt = (rule, value, callback) => {
                if (!value) {
                    callback(new Error('请选择发布时间'));
                    return;
                }
                const publishAt = toDate(value);
                const deadline = toDate(this.formData.deadline);
                if (publishAt && deadline && publishAt.getTime() > deadline.getTime()) {
                    callback(new Error('发布时间不能晚于截止时间'));
                    return;
                }
                callback();
            };
            const validateDeadline = (rule, value, callback) => {
                if (!value) {
                    callback(new Error('请选择截止时间'));
                    return;
                }
                const deadline = toDate(value);
                const signupDeadline = toDate(this.formData.signupDeadline);
                const publishAt = toDate(this.formData.publishAt);
                if (signupDeadline && deadline && signupDeadline.getTime() > deadline.getTime()) {
                    callback(new Error('报名截止时间不能晚于截止时间'));
                    return;
                }
                if (publishAt && deadline && publishAt.getTime() > deadline.getTime()) {
                    callback(new Error('发布时间不能晚于截止时间'));
                    return;
                }
                callback();
            };
            const validateSignupDeadline = (rule, value, callback) => {
                if (!value) {
                    callback(new Error('请选择报名截止时间'));
                    return;
                }
                const signupDeadline = toDate(value);
                const deadline = toDate(this.formData.deadline);
                if (signupDeadline && deadline && signupDeadline.getTime() > deadline.getTime()) {
                    callback(new Error('报名截止时间不能晚于截止时间'));
                    return;
                }
                callback();
            };
            const validateBusinessTypeId = (rule, value, callback) => {
                if (value === null || value === undefined || value === '') {
                    callback(new Error('请选择业务类型'));
                    return;
                }
                callback();
            };
            const stripEditorText = (html) => {
                if (!html) return '';
                if (typeof document === 'undefined') {
                    return String(html).replace(/<[^>]+>/g, '').trim();
                }
                const div = document.createElement('div');
                div.innerHTML = html;
                return (div.textContent || '').replace(/\u00a0/g, ' ').trim();
            };
            const validateContent = (rule, value, callback) => {
                if (!stripEditorText(value)) {
                    callback(new Error('请输入正文'));
                    return;
                }
                callback();
            };
            return {
                loading: false,
                submitting: false,
                list: [],
                total: 0,
                pageNum: 1,
                pageSize: 10,
                query: {
                    keyword: '',
                    region: '',
                    businessTypeId: undefined
                },
                businessTypeOptions: [],
                tenderEditorKey: 0,
                regionOptions: ['北京', '上海', '广东', '浙江', '江苏', '山东', '四川', '湖北', '福建', '湖南'],
                columns: [
                    { title: '标题', key: 'title', minWidth: 220 },
                    { title: '地区', key: 'region', minWidth: 100 },
                    { title: '业务类型', slot: 'businessType', minWidth: 120 },
                    { title: '项目编号', key: 'projectCode', minWidth: 130 },
                    { title: '招标单位', key: 'tenderUnit', minWidth: 180 },
                    { title: '预算', key: 'budget', minWidth: 120 },
                    { title: '状态', slot: 'status', minWidth: 90 },
                    { title: '操作', slot: 'action', minWidth: 140, align: 'center', fixed: 'right' }
                ],
                modal: {
                    show: false,
                    type: 'add'
                },
                formData: {
                    id: null,
                    title: '',
                    region: '',
                    businessTypeId: null,
                    publishAt: '',
                    content: '',
                    contactPerson: '',
                    budget: '',
                    contactPhone: '',
                    tenderUnit: '',
                    deadline: '',
                    projectCode: '',
                    signupDeadline: '',
                    status: 'PUBLISHED',
                    attachmentFileIds: []
                },
                attachments: [],
                formRules: {
                    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
                    region: [{ required: true, message: '请选择地区', trigger: 'change' }],
                    businessTypeId: [{ validator: validateBusinessTypeId, trigger: 'change' }],
                    publishAt: [{ validator: validatePublishAt, trigger: 'change' }],
                    content: [{ validator: validateContent, trigger: 'change' }],
                    contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
                    budget: [{ required: true, message: '请输入预算', trigger: 'blur' }],
                    contactPhone: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
                    tenderUnit: [{ required: true, message: '请输入招标单位', trigger: 'blur' }],
                    deadline: [{ validator: validateDeadline, trigger: 'change' }],
                    projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
                    signupDeadline: [{ validator: validateSignupDeadline, trigger: 'change' }]
                }
            };
        },
        mounted () {
            this.loadBusinessTypeOptions();
            this.getData();
        },
        methods: {
            apiErrorMessage (err) {
                if (!err) return '';
                const data = err.response && err.response.data;
                if (data && typeof data.message === 'string' && data.message) {
                    return data.message;
                }
                if (typeof err.message === 'string' && err.message) {
                    return err.message;
                }
                return '';
            },
            handleContentEditorChange () {
                this.$nextTick(() => {
                    if (this.$refs.tenderForm) {
                        this.$refs.tenderForm.validateField('content');
                    }
                });
            },
            loadBusinessTypeOptions () {
                listBusinessTypeOptions().then(res => {
                    this.businessTypeOptions = Array.isArray(res) ? res : [];
                }).catch(err => {
                    const msg = this.apiErrorMessage(err);
                    if (msg) this.$Message.error(msg);
                });
            },
            getData () {
                this.loading = true;
                listTenders({
                    pageNum: this.pageNum,
                    pageSize: this.pageSize,
                    keyword: this.query.keyword || undefined,
                    region: this.query.region || undefined,
                    businessTypeId: this.query.businessTypeId || undefined
                }).then(res => {
                    this.list = res && Array.isArray(res.list) ? res.list : [];
                    const rawTotal = res && res.total;
                    this.total = typeof rawTotal === 'number' ? rawTotal : Number(rawTotal) || 0;
                    this.loading = false;
                }).catch(err => {
                    this.loading = false;
                    const msg = this.apiErrorMessage(err);
                    if (msg) this.$Message.error(msg);
                });
            },
            handleSearch () {
                this.pageNum = 1;
                this.getData();
            },
            handleResetQuery () {
                this.query = { keyword: '', region: '', businessTypeId: undefined };
                this.pageNum = 1;
                this.getData();
            },
            handleChangePage (page) {
                this.pageNum = page;
                this.getData();
            },
            handleChangePageSize (size) {
                this.pageNum = 1;
                this.pageSize = size;
                this.getData();
            },
            handleBusinessTypeChange (value) {
                this.formData.businessTypeId = value;
                this.$nextTick(() => {
                    if (this.$refs.tenderForm) {
                        this.$refs.tenderForm.validateField('businessTypeId');
                    }
                });
            },
            handleAdd () {
                this.tenderEditorKey += 1;
                this.modal = { show: true, type: 'add' };
                this.formData = {
                    id: null,
                    title: '',
                    region: '',
                    businessTypeId: null,
                    publishAt: null,
                    content: '',
                    contactPerson: '',
                    budget: '',
                    contactPhone: '',
                    tenderUnit: '',
                    deadline: null,
                    projectCode: '',
                    signupDeadline: null,
                    status: 'PUBLISHED',
                    attachmentFileIds: []
                };
                this.attachments = [];
                this.$nextTick(() => this.$refs.tenderForm && this.$refs.tenderForm.resetFields());
            },
            handleEdit (row) {
                getTenderDetail(row.id).then(res => {
                    this.tenderEditorKey += 1;
                    this.modal = { show: true, type: 'edit' };
                    this.formData = {
                        id: res.id,
                        title: res.title || '',
                        region: res.region || '',
                        businessTypeId: res.businessType ? res.businessType.id : null,
                        publishAt: this.parseBackendDateTime(res.publishAt),
                        content: res.content || '',
                        contactPerson: res.contactPerson || '',
                        budget: res.budget || '',
                        contactPhone: res.contactPhone || '',
                        tenderUnit: res.tenderUnit || '',
                        deadline: this.parseBackendDateTime(res.deadline),
                        projectCode: res.projectCode || '',
                        signupDeadline: this.parseBackendDateTime(res.signupDeadline),
                        status: res.status || 'PUBLISHED',
                        attachmentFileIds: Array.isArray(res.attachments) ? res.attachments.map(item => item.fileId) : []
                    };
                    this.attachments = Array.isArray(res.attachments)
                        ? res.attachments.map(item => ({ fileId: item.fileId, fileName: item.fileName, localUrl: '' }))
                        : [];
                    this.$nextTick(() => this.$refs.tenderForm && this.$refs.tenderForm.clearValidate());
                }).catch(err => {
                    const msg = this.apiErrorMessage(err);
                    if (msg) this.$Message.error(msg);
                });
            },
            handleSubmit () {
                this.$refs.tenderForm.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    const payload = {
                        title: this.formData.title,
                        region: this.formData.region,
                        businessTypeId: this.formData.businessTypeId,
                        publishAt: this.formatDateTime(this.formData.publishAt),
                        content: this.formData.content,
                        contactPerson: this.formData.contactPerson,
                        budget: this.formData.budget,
                        contactPhone: this.formData.contactPhone,
                        tenderUnit: this.formData.tenderUnit,
                        deadline: this.formatDateTime(this.formData.deadline),
                        projectCode: this.formData.projectCode,
                        signupDeadline: this.formatDateTime(this.formData.signupDeadline),
                        status: this.formData.status,
                        attachmentFileIds: this.formData.attachmentFileIds
                    };
                    const req = this.modal.type === 'add'
                        ? createTender(payload)
                        : updateTender(this.formData.id, payload);
                    const wasAdd = this.modal.type === 'add';
                    req.then(() => {
                        this.submitting = false;
                        this.modal.show = false;
                        this.$Message.success(wasAdd ? '新增招标成功' : '更新招标成功');
                        this.getData();
                    }).catch(err => {
                        this.submitting = false;
                        const msg = this.apiErrorMessage(err);
                        if (msg) this.$Message.error(msg);
                    });
                });
            },
            handleDelete (row) {
                this.$Modal.confirm({
                    title: '确认删除',
                    content: `确定删除招标「${row.title}」吗？`,
                    onOk: () => deleteTender(row.id).then(() => {
                        this.$Message.success('删除成功');
                        this.getData();
                    })
                });
            },
            handleBeforeUpload (file) {
                uploadTenderFiles([file]).then(res => {
                    const files = Array.isArray(res) ? res : [];
                    files.forEach(item => {
                        if (!item || item.fileId == null) return;
                        if (this.formData.attachmentFileIds.includes(item.fileId)) return;
                        this.formData.attachmentFileIds.push(item.fileId);
                        this.attachments.push({
                            fileId: item.fileId,
                            fileName: item.fileName,
                            localUrl: window.URL.createObjectURL(file)
                        });
                    });
                    this.$Message.success('附件上传成功');
                }).catch(err => {
                    const msg = this.apiErrorMessage(err);
                    if (msg) this.$Message.error(msg);
                });
                return false;
            },
            previewAttachment (item) {
                if (item && item.localUrl) {
                    window.open(item.localUrl, '_blank');
                    return;
                }
                this.$Message.info('当前仅支持预览本次新增上传的附件');
            },
            removeAttachment (fileId) {
                const removed = this.attachments.find(item => item.fileId === fileId);
                if (removed && removed.localUrl) {
                    window.URL.revokeObjectURL(removed.localUrl);
                }
                this.formData.attachmentFileIds = this.formData.attachmentFileIds.filter(id => id !== fileId);
                this.attachments = this.attachments.filter(item => item.fileId !== fileId);
            },
            parseBackendDateTime (value) {
                if (!value) return null;
                if (value instanceof Date) return value;
                const parsed = new Date(String(value).replace(' ', 'T'));
                return Number.isNaN(parsed.getTime()) ? null : parsed;
            },
            formatDateTime (value) {
                if (!value) return '';
                const date = value instanceof Date ? value : this.parseBackendDateTime(value);
                if (!date) return '';
                const pad = num => String(num).padStart(2, '0');
                return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
            }
        }
    };
</script>

<style scoped>
  .attachment-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }

  .attachment-remove {
    color: #808695;
    cursor: pointer;
  }
</style>
