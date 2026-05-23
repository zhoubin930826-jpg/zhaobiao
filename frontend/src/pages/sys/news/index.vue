<template>
  <div>
    <div class="i-layout-page-header">
      <PageHeader title="资讯管理" hidden-breadcrumb />
    </div>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <Form :model="query" inline>
        <FormItem>
          <Input v-model="query.keyword" placeholder="标题/摘要关键词" clearable />
        </FormItem>
        <FormItem>
          <Select v-model="query.category" clearable placeholder="资讯分类" style="width: 160px">
            <Option v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ item.label }}</Option>
          </Select>
        </FormItem>
        <FormItem>
          <Select v-model="query.status" clearable placeholder="状态" style="width: 120px">
            <Option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</Option>
          </Select>
        </FormItem>
        <FormItem>
          <Button type="primary" @click="handleSearch">查询</Button>
          <Button class="ivu-ml-8" @click="handleResetQuery">重置</Button>
        </FormItem>
      </Form>

      <div class="ivu-mt-8 ivu-mb-8">
        <Button v-auth="['NEWS_CREATE_BUTTON']" type="primary" @click="handleAdd">
          <Icon type="md-add" /> 新增资讯
        </Button>
      </div>

      <Table :columns="columns" :data="list" :loading="loading">
        <template slot="category" slot-scope="{ row }">
          {{ row.categoryLabel || row.category || '—' }}
        </template>
        <template slot="publishAt" slot-scope="{ row }">
          {{ formatDateTimeDisplay(row.publishAt) }}
        </template>
        <template slot="status" slot-scope="{ row }">
          <Badge v-if="row.status === 'PUBLISHED'" color="green" :text="row.statusLabel || '已发布'" />
          <Badge v-else-if="row.status === 'DRAFT'" color="default" :text="row.statusLabel || '草稿'" />
          <span v-else>{{ row.statusLabel || row.status || '—' }}</span>
        </template>
        <template slot="action" slot-scope="{ row }">
          <div @click.stop.prevent>
            <template v-if="row.status === 'DRAFT'">
              <a v-auth="['NEWS_EDIT_BUTTON']" @click="handleEdit(row)">编辑</a>
              <span v-auth="['NEWS_PUBLISH_BUTTON']">
                <Divider type="vertical" />
                <a @click="handlePublish(row)">发布</a>
              </span>
              <span v-auth="['NEWS_DELETE_BUTTON']">
                <Divider type="vertical" />
                <a style="color: #ed4014" @click="handleDelete(row)">删除</a>
              </span>
            </template>
            <template v-else-if="row.status === 'PUBLISHED'">
              <span v-auth="['NEWS_PUBLISH_BUTTON']">
                <a @click="handleUnpublish(row)">取消发布</a>
              </span>
              <span v-if="!hasPublishPermission" class="action-muted">已发布（不可编辑）</span>
            </template>
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
        :title="modal.type === 'add' ? '新增资讯' : '编辑资讯'"
        width="920"
        :transfer="false"
      >
        <Form ref="newsForm" :model="formData" :rules="formRules" label-position="top">
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="标题" prop="title">
                <Input v-model="formData.title" maxlength="200" placeholder="请输入资讯标题" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="资讯分类" prop="category">
                <Select v-model="formData.category" placeholder="请选择资讯分类">
                  <Option v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ item.label }}</Option>
                </Select>
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
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
            <Col span="12">
              <FormItem label="信息来源" prop="source">
                <Input v-model="formData.source" maxlength="128" placeholder="请输入信息来源" />
              </FormItem>
            </Col>
          </Row>
          <FormItem label="内容总结" prop="summary">
            <Input
              v-model="formData.summary"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入内容总结（列表页展示）"
            />
          </FormItem>
          <FormItem label="正文" prop="content">
            <i-quill-editor
              v-if="modal.show"
              :key="'news-editor-' + newsEditorKey"
              v-model="formData.content"
              border
              :min-height="280"
              placeholder="请输入资讯正文（富文本）"
              @on-change="handleContentEditorChange"
            />
          </FormItem>
          <FormItem label="封面图片">
            <Upload
              v-auth="['NEWS_UPLOAD_BUTTON']"
              type="drag"
              :before-upload="handleBeforeCoverUpload"
              :show-upload-list="false"
            >
              <div style="padding: 12px 0">
                <Icon type="ios-cloud-upload" size="28" style="color: #2d8cf0"></Icon>
                <p>点击或拖拽图片到此处上传封面</p>
              </div>
            </Upload>
            <div v-if="coverFile" class="ivu-mt-8 cover-card">
              <img v-if="coverPreviewUrl" :src="coverPreviewUrl" alt="封面预览" class="cover-thumb" />
              <div class="cover-meta">
                <div class="cover-name">{{ coverFile.fileName || (`文件#${coverFile.fileId}`) }}</div>
                <div class="cover-actions">
                  <Button type="primary" ghost size="small" icon="ios-eye-outline" @click="previewCover">查看</Button>
                  <Button type="error" ghost size="small" icon="md-trash" @click="removeCover">移除</Button>
                </div>
              </div>
            </div>
            <p v-else class="cover-empty">未上传封面（可选）</p>
          </FormItem>
        </Form>
        <template #footer>
          <Button @click="modal.show = false">取消</Button>
          <Button type="primary" :loading="submitting" @click="handleSubmit">保存</Button>
        </template>
      </Modal>

      <div v-if="viewer.open" class="preview-mask" @click.self="closeViewer">
        <div class="preview-dialog">
          <header class="preview-header">
            <span class="preview-title">{{ viewer.title }}</span>
            <Button size="small" @click="closeViewer">关闭</Button>
          </header>
          <div class="preview-body">
            <img v-if="viewer.src" :src="viewer.src" alt="封面预览" />
          </div>
        </div>
      </div>
    </Card>
  </div>
</template>

<script>
    import IQuillEditor from '@/components/quill-editor';
    import Setting from '@/setting';
    import {
        listNews,
        getNewsDetail,
        createNews,
        updateNews,
        updateNewsStatus,
        deleteNews,
        uploadAdminFiles
    } from '@api/system';

    const CATEGORY_OPTIONS = [
        { value: 'PLATFORM_NOTICE', label: '平台公告' },
        { value: 'INDUSTRY_NEWS', label: '行业动态' },
        { value: 'SERVICE_GUIDE', label: '办事指南' },
        { value: 'POLICY_REGULATION', label: '政策法规' }
    ];

    const STATUS_OPTIONS = [
        { value: 'DRAFT', label: '草稿' },
        { value: 'PUBLISHED', label: '已发布' }
    ];

    export default {
        name: 'system-news',
        components: {
            IQuillEditor
        },
        data () {
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
                    category: undefined,
                    status: undefined
                },
                categoryOptions: CATEGORY_OPTIONS,
                statusOptions: STATUS_OPTIONS,
                newsEditorKey: 0,
                columns: [
                    { title: '标题', key: 'title', minWidth: 220 },
                    { title: '分类', slot: 'category', minWidth: 110 },
                    { title: '信息来源', key: 'source', minWidth: 140 },
                    { title: '发布时间', slot: 'publishAt', minWidth: 170 },
                    { title: '状态', slot: 'status', minWidth: 100 },
                    { title: '操作', slot: 'action', minWidth: 200, align: 'center', fixed: 'right' }
                ],
                modal: {
                    show: false,
                    type: 'add'
                },
                formData: {
                    id: null,
                    title: '',
                    category: '',
                    publishAt: null,
                    source: '',
                    summary: '',
                    content: '',
                    status: 'DRAFT',
                    coverFileId: null
                },
                coverFile: null,
                coverLocalUrl: '',
                viewer: {
                    open: false,
                    src: '',
                    title: ''
                },
                formRules: {
                    title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
                    category: [{ required: true, message: '请选择资讯分类', trigger: 'change' }],
                    publishAt: [{ required: true, type: 'date', message: '请选择发布时间', trigger: 'change' }],
                    source: [{ required: true, message: '请输入信息来源', trigger: 'blur' }],
                    summary: [{ required: true, message: '请输入内容总结', trigger: 'blur' }],
                    content: [{ validator: validateContent, trigger: 'change' }]
                }
            };
        },
        computed: {
            coverPreviewUrl () {
                if (!this.coverFile) return '';
                if (this.coverLocalUrl) return this.coverLocalUrl;
                if (this.coverFile.thumbnailUrl) return this.withApiBase(this.coverFile.thumbnailUrl);
                if (this.coverFile.fileId != null) {
                    return this.withApiBase(`/api/files/${this.coverFile.fileId}/thumbnail`);
                }
                return '';
            },
            canPublishNews () {
                const info = this.$store.state.admin.user.info || {};
                const access = info.access || info.permissions || [];
                return access.includes('NEWS_PUBLISH_BUTTON');
            },
            hasPublishPermission () {
                return this.canPublishNews;
            }
        },
        mounted () {
            this.getData();
        },
        beforeDestroy () {
            this.revokeCoverLocalUrl();
            this.closeViewer();
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
            withApiBase (path) {
                if (!path) return '';
                if (/^https?:\/\//i.test(path)) return path;
                const raw = String(path);
                if (raw.startsWith('/api/')) return raw;
                const base = (Setting.apiBaseURL || '').replace(/\/+$/, '');
                const cleaned = raw.replace(/^\/+/, '');
                return `${base}/${cleaned}`;
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
            },
            formatDateTimeDisplay (value) {
                if (!value) return '—';
                return String(value).replace('T', ' ').slice(0, 19);
            },
            buildPayload (data) {
                const payload = {
                    title: data.title,
                    category: data.category,
                    publishAt: this.formatDateTime(data.publishAt),
                    source: data.source,
                    summary: data.summary,
                    content: data.content,
                    coverFileId: data.coverFileId
                };
                if (this.modal.type === 'edit') {
                    payload.status = data.status;
                }
                return payload;
            },
            resetCover () {
                this.revokeCoverLocalUrl();
                this.coverFile = null;
                this.formData.coverFileId = null;
            },
            revokeCoverLocalUrl () {
                if (this.coverLocalUrl) {
                    window.URL.revokeObjectURL(this.coverLocalUrl);
                    this.coverLocalUrl = '';
                }
            },
            setCoverFromUpload (item, file, localUrl) {
                this.coverFile = {
                    fileId: item.fileId,
                    fileName: item.fileName || (file && file.name) || '封面',
                    thumbnailUrl: item.thumbnailUrl || ''
                };
                this.formData.coverFileId = item.fileId;
                this.revokeCoverLocalUrl();
                this.coverLocalUrl = localUrl || '';
            },
            setCoverFromDetail (res) {
                this.revokeCoverLocalUrl();
                if (res.coverFileId != null) {
                    this.coverFile = {
                        fileId: res.coverFileId,
                        fileName: '封面图片',
                        thumbnailUrl: res.coverUrl || ''
                    };
                    this.formData.coverFileId = res.coverFileId;
                } else {
                    this.coverFile = null;
                    this.formData.coverFileId = null;
                }
            },
            handleContentEditorChange () {
                this.$nextTick(() => {
                    if (this.$refs.newsForm) {
                        this.$refs.newsForm.validateField('content');
                    }
                });
            },
            getData () {
                this.loading = true;
                listNews({
                    pageNum: this.pageNum,
                    pageSize: this.pageSize,
                    keyword: this.query.keyword || undefined,
                    category: this.query.category || undefined,
                    status: this.query.status || undefined
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
                this.query = { keyword: '', category: undefined, status: undefined };
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
            handleAdd () {
                this.newsEditorKey += 1;
                this.modal = { show: true, type: 'add' };
                this.formData = {
                    id: null,
                    title: '',
                    category: '',
                    publishAt: null,
                    source: '',
                    summary: '',
                    content: '',
                    status: 'DRAFT',
                    coverFileId: null
                };
                this.resetCover();
                this.$nextTick(() => this.$refs.newsForm && this.$refs.newsForm.resetFields());
            },
            handleEdit (row) {
                if (row.status === 'PUBLISHED') {
                    this.$Message.warning('已发布资讯请先取消发布后再编辑');
                    return;
                }
                getNewsDetail(row.id).then(res => {
                    this.newsEditorKey += 1;
                    this.modal = { show: true, type: 'edit' };
                    this.formData = {
                        id: res.id,
                        title: res.title || '',
                        category: res.category || '',
                        publishAt: this.parseBackendDateTime(res.publishAt),
                        source: res.source || '',
                        summary: res.summary || '',
                        content: res.content || '',
                        status: res.status || 'DRAFT',
                        coverFileId: res.coverFileId != null ? res.coverFileId : null
                    };
                    this.setCoverFromDetail(res);
                    this.$nextTick(() => this.$refs.newsForm && this.$refs.newsForm.clearValidate());
                }).catch(err => {
                    const msg = this.apiErrorMessage(err);
                    if (msg) this.$Message.error(msg);
                });
            },
            handleSubmit () {
                this.$refs.newsForm.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    const payload = this.buildPayload(this.formData);
                    const req = this.modal.type === 'add'
                        ? createNews(payload)
                        : updateNews(this.formData.id, payload);
                    const wasAdd = this.modal.type === 'add';
                    req.then(() => {
                        this.submitting = false;
                        this.modal.show = false;
                        this.$Message.success(wasAdd ? '新增资讯成功（草稿）' : '更新资讯成功');
                        this.getData();
                    }).catch(err => {
                        this.submitting = false;
                        const msg = this.apiErrorMessage(err);
                        if (msg) this.$Message.error(msg);
                    });
                });
            },
            handlePublish (row) {
                this.$Modal.confirm({
                    title: '确认发布',
                    content: `确定发布资讯「${row.title}」吗？`,
                    onOk: () => updateNewsStatus(row.id, { status: 'PUBLISHED' }).then(() => {
                        this.$Message.success('发布成功');
                        this.getData();
                    }).catch(err => {
                        const msg = this.apiErrorMessage(err);
                        if (msg) this.$Message.error(msg);
                        return Promise.reject(err);
                    })
                });
            },
            handleUnpublish (row) {
                this.$Modal.confirm({
                    title: '确认取消发布',
                    content: `确定将资讯「${row.title}」取消发布吗？取消发布后可再次编辑。`,
                    onOk: () => updateNewsStatus(row.id, { status: 'DRAFT' }).then(() => {
                        this.$Message.success('已取消发布');
                        this.getData();
                    }).catch(err => {
                        const msg = this.apiErrorMessage(err);
                        if (msg) this.$Message.error(msg);
                        return Promise.reject(err);
                    })
                });
            },
            handleDelete (row) {
                if (row.status === 'PUBLISHED') {
                    this.$Message.warning('已发布资讯请先取消发布后再删除');
                    return;
                }
                this.$Modal.confirm({
                    title: '确认删除',
                    content: `确定删除资讯「${row.title}」吗？`,
                    onOk: () => deleteNews(row.id).then(() => {
                        this.$Message.success('删除成功');
                        this.getData();
                    }).catch(err => {
                        const msg = this.apiErrorMessage(err);
                        if (msg) this.$Message.error(msg);
                        return Promise.reject(err);
                    })
                });
            },
            handleBeforeCoverUpload (file) {
                const localUrl = window.URL.createObjectURL(file);
                uploadAdminFiles([file]).then(res => {
                    const files = Array.isArray(res) ? res : [];
                    const item = files.find(entry => entry && entry.fileId != null);
                    if (!item) {
                        window.URL.revokeObjectURL(localUrl);
                        this.$Message.error('封面上传失败');
                        return;
                    }
                    this.setCoverFromUpload(item, file, localUrl);
                    this.$Message.success('封面上传成功');
                }).catch(err => {
                    window.URL.revokeObjectURL(localUrl);
                    const msg = this.apiErrorMessage(err);
                    if (msg) this.$Message.error(msg);
                });
                return false;
            },
            previewCover () {
                const url = this.coverPreviewUrl;
                if (!url) {
                    this.$Message.info('暂无可预览的封面');
                    return;
                }
                this.viewer.title = this.coverFile.fileName || '封面预览';
                this.viewer.src = url;
                this.viewer.open = true;
            },
            removeCover () {
                this.resetCover();
            },
            closeViewer () {
                this.viewer.open = false;
                this.viewer.src = '';
                this.viewer.title = '';
            }
        }
    };
</script>

<style scoped>
  .cover-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    background: #f8fafc;
    border: 1px solid #e8eaec;
    border-radius: 6px;
  }

  .cover-thumb {
    width: 96px;
    height: 72px;
    object-fit: cover;
    border-radius: 4px;
    border: 1px solid #e2e8f0;
    flex-shrink: 0;
  }

  .cover-meta {
    flex: 1;
    min-width: 0;
  }

  .cover-name {
    font-size: 13px;
    font-weight: 600;
    color: #17233d;
    margin-bottom: 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .cover-actions {
    display: flex;
    gap: 8px;
  }

  .cover-empty {
    margin: 8px 0 0;
    font-size: 12px;
    color: #808695;
  }

  .action-muted {
    color: #808695;
    font-size: 12px;
  }

  .preview-mask {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.45);
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 1.2rem;
    z-index: 1100;
  }

  .preview-dialog {
    width: min(900px, 95vw);
    max-height: 80vh;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 16px 48px rgba(15, 23, 42, 0.18);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .preview-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.85rem 1rem;
    border-bottom: 1px solid #e2e8f0;
    background: #f8fafc;
  }

  .preview-title {
    font-weight: 600;
    color: #1f2937;
  }

  .preview-body {
    flex: 1;
    overflow: auto;
    padding: 0.75rem;
    background: #0f172a;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .preview-body img {
    max-width: 100%;
    max-height: 70vh;
    object-fit: contain;
  }

  ::v-deep .ivu-modal-content {
    display: flex;
    flex-direction: column;
  }

  ::v-deep .ivu-modal-body {
    flex: 1;
    max-height: calc(100vh - 220px);
    overflow-y: auto;
  }
</style>
