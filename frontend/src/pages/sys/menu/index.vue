<template>
  <div>
    <div class="i-layout-page-header">
      <!-- <PageHeader title="组织管理" hidden-breadcrumb /> -->
    </div>
    <Row :gutter="24" class="ivu-mt">
      <Col :xl="10" :lg="24" :md="24" :sm="24" :xs="24" class="ivu-mb">
        <Affix :offset-top="108">
          <Card :bordered="false" dis-hover class="ivu-mb">
            <div slot="title">
              <Dropdown>
                <Button type="primary" ghost>
                  添加组织
                  <Icon type="ios-arrow-down"></Icon>
                </Button>
                <DropdownMenu slot="list">
                  <DropdownItem @click.native="handleOpenCreateSider"
                    >添加子组织</DropdownItem
                  >
                </DropdownMenu>
              </Dropdown>
              <Button
                type="primary"
                ghost
                class="ivu-ml"
                v-show="!toggleExpand"
                @click="handleToggleExpandAll(true)"
              >
                <Icon type="md-list" />
                全部展开
              </Button>
              <Button
                type="primary"
                ghost
                class="ivu-ml"
                v-show="toggleExpand"
                @click="handleToggleExpandAll(false)"
              >
                <Icon type="md-list" />
                全部收起
              </Button>
            </div>
            <div :style="menuListStyle">
              <div class="ivu-p-8">
                <!-- <Input
                  v-if="menuList.length || query !== ''"
                  v-model="query"
                  search
                  placeholder="输入菜单名称搜索"
                  @on-change="handleSearch"
                /> -->
              </div>
              <Tree
                @on-select-change="handleSelectNode"
                :data="menuList"
                :children-key="`child`"
                :render="renderContent"
              >
              </Tree>
            </div>
          </Card>
        </Affix>
      </Col>
      <Col :xl="14" :lg="24" :md="24" :sm="24" :xs="24" class="ivu-mb">
        <Card :bordered="false" dis-hover class="ivu-mb">
          <div slot="title">
            <Avatar icon="md-apps" size="small" :color="'#1890ff'" />
            <span class="ivu-pl-8">角色人员列表</span>
          </div>
          <div>
          </div>
          <div v-for="(item, index) in tableList" :key="index">
            <h3>
              {{ item.name }}
            </h3>
            <Table
              ref="table"
              :columns="tableColumns"
              :data="item.user_list"
              :loading="loading"
              :size="tableSize"
              class="ivu-mt"
            >
              <template slot-scope="{ row }" slot="status">
                <div>{{ row.status == 1 ? '正常' : '冻结' }}</div>
              </template>
            </Table>
          </div>
        </Card>
      </Col>
    </Row>

    <Modal
      v-model="modal.show"
      :before-close="handleCloseEdit"
      :transfer="false"
      width="500"
    >
      <div slot="header" class="ivu-modal-header-inner">
        <template v-if="modal.type === 'header'">添加顶栏菜单</template>
        <template v-if="modal.type === 'sider'">
          添加子组织<span v-if="menuInfo">{{ menuInfo.title }}</span>
        </template>
      </div>
      <Form
        v-if="menuData"
        ref="menuForm"
        :model="menuData"
        :label-width="100"
      >
        <FormItem prop="title" label="组织名称">
          <Input v-model="menuData.title" placeholder="请输入组织名称，必填" />
        </FormItem>
        <FormItem prop="status" label="启用状态">
          <RadioGroup v-model="menuData.status">
            <Radio label="1">启用</Radio>
            <Radio label="0">停用</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem prop="subtitle" label="排序" v-if="modal.type === 'sider'">
          <Input v-model="menuData.sort" placeholder="请输入序号" />
        </FormItem>
        <FormItem prop="remark" label="备注" v-if="modal.type === 'sider'">
          <Input v-model="menuData.remark" placeholder="备注" />
        </FormItem>
      </Form>
      <template #footer>
        <Button v-if="modal.type === 'sider'" type="primary" @click="handleSubmitSider" :loading="submitting">提交</Button>
      </template>
    </Modal>

    <Modal v-model="OrgModal" title="编辑组织" @on-ok="ok" @on-cancel="cancel">
      <Form
        :model="OrgInfo"
        ref="formInline"
        :rules="ruleInline"
        :label-width="100"
      >
        <FormItem label="组织名称">
          <Input v-model="OrgInfo.title"></Input>
        </FormItem>
        <FormItem label="启用状态">
          <RadioGroup v-model="OrgInfo.status">
            <Radio label="1">启用</Radio>
            <Radio label="0">停用</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem label="排序">
          <Input v-model="OrgInfo.sort"></Input>
        </FormItem>
        <FormItem label="备注">
          <Input v-model="OrgInfo.remark"></Input>
        </FormItem>
      </Form>
    </Modal>
  </div>
</template>

<script>
    import {
        GetMenuList,
        addOrg,
        editOrg,
        DeleteOrgMultiple,
        UpdateMenu
    } from '@api/system';
    import { menuDtosToSysTree } from '@/libs/bid-menu';
    import { cloneDeep } from 'lodash';

    const siderMenu = {
        title: '',
        status: '',
        level: '',
        pid: '',
        icon: '',
        fe_path: '',
        api_path: '',
        sort: ''
    };

    export default {
        name: 'system-menu',
        data () {
            return {
                columns: [
                    {
                        title: '姓名',
                        key: 'realname',
                        show: true
                    },
                    {
                        title: '手机号',
                        key: 'phone',
                        show: true
                    },
                    {
                        title: '状态',
                        key: 'status',
                        slot: 'status',
                        show: true
                    }
                ],
                ruleInline: {},
                OrgInfo: {},
                OrgModal: false,
                tableList: [],
                // loading: false,
                tableSize: 'default',
                tableData: [],
                loading: false,
                sourceMenuList: [],
                menuList: [],
                selectedData: [],
                toggleExpand: true,
                menuInfo: {},
                contextMenuInfo: null,
                query: '',
                modal: {
                    show: false,
                    type: 'header' // header || sider
                },
                submitting: false,
                menuData: null,
                menuDataString: '',
                editMenuData: null,
                editMenuReady: true
            };
        },
        computed: {
            tableColumns () {
                const columns = [...this.columns];
                return columns.filter(item => item.show);
            },
            menuListStyle () {
                return {
                    height: document.body.clientHeight - 330 + 'px',
                    overflow: 'auto'
                };
            }
        },
        methods: {
            // 获取菜单数据
            handleGetMenuList () {
                GetMenuList().then(res => {
                    const tree = menuDtosToSysTree(Array.isArray(res) ? res : []);
                    this.sourceMenuList = [
                        {
                            title: '系统菜单',
                            child: tree,
                            pid: null,
                            level: 1
                        }
                    ];
                    this.menuList = cloneDeep(this.sourceMenuList);
                    this.handleToggleExpandAll(true);
                });
            },
            // 清空所有已选项
            handleClearSelect () {
                this.selectedData = [];
            },
            // 展开所有树
            handleToggleExpandAll (status) {
                this.toggleExpand = status;
                this.menuList = this.handleToggleExpandNode(this.menuList, status, []);
            },
            /**
             * @description 递归状态
             * @param {Array} menuList 原数据
             * @param {Boolean} isExpand 是否展开
             * @param {Array} lastList 递归数据
             * */
            handleToggleExpandNode (menuList, isExpand, lastList) {
                menuList.forEach(menu => {
                    let newMenu = {};
                    for (let i in menu) {
                        if (i !== 'children' && i !== 'child') newMenu[i] = cloneDeep(menu[i]);
                    }
                    newMenu.expand = isExpand;
                    newMenu.contextmenu = true;
                    const sub = menu.child || menu.children;
                    if (sub && sub.length) newMenu.child = [];

                    lastList.push(newMenu);
                    sub &&
                        this.handleToggleExpandNode(
                            sub,
                            isExpand,
                            newMenu.child
                    );
                });
                return lastList;
            },
            handleSelectNode (item) {
                if (item.length) {
                    this.selectedData = item;
                    this.menuInfo = item[0];
                    this.tableList = [];
                }
            },
            handleCheckNode (items) {
                this.selectedData = items;
            },
            handleContextMenu (item) {
                this.contextMenuInfo = item;
            },
            handleContextMenuEdit () {
                this.menuInfo = this.contextMenuInfo;
                this.handleInitEditMenuData();
                this.menuList = this.handleSelectNodeById(
                    this.menuList,
                    this.menuInfo.id,
                    []
                );
            },
            handleSelectNodeById (menuList, id, lastList) {
                menuList.forEach(menu => {
                    let newMenu = {};
                    for (let i in menu) {
                        if (i !== 'children' && i !== 'child') newMenu[i] = cloneDeep(menu[i]);
                    }

                    newMenu.selected = newMenu.id === id;
                    const sub = menu.child || menu.children;
                    if (sub && sub.length) newMenu.child = [];

                    lastList.push(newMenu);
                    sub &&
                        this.handleSelectNodeById(sub, id, newMenu.child);
                });
                return lastList;
            },
            handleSearch () {
                const query = this.query;
                this.menuInfo = null;
                this.selectedData = [];
                this.contextMenuInfo = null;
                if (query) {
                    this.menuList = this.handleQueryNode(this.sourceMenuList, query, []);
                } else {
                    this.handleInitMenuList();
                }
            },
            handleInitMenuList () {
                this.menuList = cloneDeep(this.sourceMenuList);
                this.handleToggleExpandAll(true);
            },
            handleQueryNode (menuList, query, queryList) {
                menuList.forEach(menu => {
                    let newMenu = {};
                    for (let i in menu) {
                        if (i !== 'child') newMenu[i] = cloneDeep(menu[i]);
                    }
                    if (newMenu.title.indexOf(query) > -1) queryList.push(newMenu);
                    if (menu.child && menu.child.length) newMenu.child = [];
                    menu.child && this.handleQueryNode(menu.child, query, queryList);
                });
                return queryList;
            },
            handleOpenCreateSider () {
                this.menuData = cloneDeep(siderMenu);
                this.menuDataString = JSON.stringify(this.menuData);
                this.modal.type = 'sider';
                this.modal.show = true;
            },
            handleContextMenuNew () {
                this.menuInfo = this.contextMenuInfo;
                this.handleInitEditMenuData();
                this.menuList = this.handleSelectNodeById(
                    this.menuList,
                    this.menuInfo.id,
                    []
                );
                this.handleOpenCreateSider();
            },
            handleCloseEdit () {
                // 判断内容是否修改，没修改则直接关闭
                const editMenuData = cloneDeep(this.menuData);
                const editMenuDataString = JSON.stringify(editMenuData);

                if (editMenuDataString !== this.menuDataString) {
                    return new Promise(resolve => {
                        this.$Modal.confirm({
                            title: '关闭确认',
                            content: '您已经编辑了菜单信息，是否直接关闭？',
                            onOk: () => {
                                this.menuData = null;
                                resolve();
                            }
                        });
                    });
                } else {
                    this.menuData = null;
                }
            },
            handleSubmitSider () {
                // 当存在时表示增加的时某一个子菜单
                if (this.menuInfo.id) {
                    this.menuData.pid = this.menuInfo.id;
                    this.menuData.level = this.menuInfo.level - 0 + 1;
                } else {
                    this.menuData.pid = 0;
                    this.menuData.level = 2;
                }
                this.$refs.menuForm.validate(valid => {
                    if (valid) {
                        this.submitting = true;
                        addOrg({
                            title: this.menuData.title,
                            pid: this.menuData.pid,
                            level: this.menuData.level,
                            status: this.menuData.status,
                            remark: this.menuData.remark,
                            sort: this.menuData.sort
                        }).then(res => {
                            this.submitting = false;
                            this.modal.show = false;
                            this.menuData = null;
                            this.$Message.success('创建成功');
                            // 完成后刷新数据
                            this.handleClearSelect();
                            this.handleGetMenuList();
                        }).catch(e => {
                            this.submitting = false;
                        });
                    }
                });
            },
            renderContent (h, { root, node, data }) {
                return h(
                    'span',
                    {
                        style: {
                            display: 'inline-block',
                            width: '100%'
                        }
                    },
                    [
                        h('span', [h('span', data.title)]),
                        h(
                            'span',
                            {
                                style: {
                                    display: 'inline',
                                    float: 'right',
                                    marginLeft: '12px',
                                    marginRight: '32px'
                                }
                            },
                            [
                                h(
                                    'a',
                                    {
                                        style: {
                                            display: data.pid === null ? 'none' : '',
                                            marginRight: '8px'
                                        },
                                        on: {
                                            click: () => {
                                                this.edit(data);
                                            }
                                        }
                                    },
                                    '编辑'
                                ),
                                h(
                                    'a',
                                    {
                                        style: {
                                            display: data.pid === null ? 'none' : ''
                                            // marginRight: '8px'
                                        },
                                        on: {
                                            click: () => {
                                                this.romoveOrg(data);
                                            }
                                        }
                                    },
                                    '删除'
                                )
                            ]
                        )
                    ]
                );
            },
            handleInitEditMenuData () {
                let menuData = null;
                if (this.menuInfo) {
                    if (this.menuInfo.name) {
                        // menuData = cloneDeep(headerMenu);
                    } else {
                        menuData = cloneDeep(siderMenu);
                    }
                    for (let i in menuData) {
                        if (i in this.menuInfo) menuData[i] = this.menuInfo[i];
                    }
                }
                this.editMenuData = menuData;
                this.editMenuReady = false;
                this.$nextTick(() => {
                    this.editMenuReady = true;
                });
            },
            // 编辑组织操作
            edit (data) {
                this.OrgInfo = { ...data, title: data.title || data.name };
                this.OrgModal = true;
            },
            // 编辑保存操作
            ok () {
                this.$refs.formInline.validate(valid => {
                    if (valid) {
                        editOrg(this.OrgInfo).then(res => {
                            this.$Message.success('编辑成功');
                            this.handleGetMenuList();
                            // this.handleToggleExpandAll(true)
                        });
                    }
                });
            },
            // 取消操作
            cancel () {},
            // 删除组织操作
            romoveOrg (data) {
                this.$Modal.confirm({
                    title: '删除组织',
                    content: '是否确认删除当前组织',
                    okText: '确定',
                    cancelText: '取消',
                    loading: true,
                    onOk: () => {
                        DeleteOrgMultiple({
                            ids: data.id
                        }).then(res => {
                            this.$Modal.remove();
                            this.$Message.success('编辑成功');
                            this.handleGetMenuList();
                        });
                    }
                });

                console.log(data);
            },

            // 修改菜单
            handleSubmitUpdate () {
                this.$refs.infoForm.validate(valid => {
                    if (valid) {
                        if (this.submitting) return;
                        this.submitting = true;
                        UpdateMenu({
                            id: this.menuInfo.id,
                            ...this.editMenuData
                        }).then(res => {
                            this.submitting = false;
                            this.$Message.success('编辑成功');
                            // 成功后操作
                            this.handleGetMenuList();
                        });
                    }
                });
            },
            handleResetUpdate () {
                this.$refs.infoForm.resetFields();
            }
        },
        mounted () {
            this.handleGetMenuList();
        }
    };
</script>

<style lang="less" scoped></style>
