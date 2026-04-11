<template>
  <Modal width='80' v-model="showModal" :title="title" footer-hide>
      <Table
        ref="table"
        :columns="tableColumns"
        :data="list"
        :loading="loading"
        :size="tableSize"
        class="ivu-mt"
      >
      <template v-for='item in slotKeys'   slot-scope="{ row }" :slot="item">
            <slot   v-bind='row' :name='item'></slot>
        </template>
        <template   slot-scope="{ row }" slot="action">
            <slot v-bind='row' name='action'></slot>
        </template>
      </Table>
      <div class="ivu-mt ivu-text-center">
        <Page
          :total="total"
          :current.sync="current"
          show-total
          show-sizer
          show-elevator
          :page-size="limit"
          @on-change="handleChangePage"
          @on-page-size-change="handleChangePageSize"
        />
      </div>
    </Modal>
</template>

<script>
    export default {
        name: 'SimpleTable',
        props: {
            show: {
                type: Boolean,
                default: false
            },
            columns: {
                type: Array,
                default: () => []
            },
            title: {
                type: String,
                default: ''
            },
            getList: {
                type: Function,
                default: null
            },
            id: {
                type: [String, Number],
                default: null
            },
            slotKeys: {
                type: Array,
                default: () => []
            }
        },
        data () {
            return {
                list: [],
                tableSize: 'default',
                current: 1,
                limit: 10,
                total: 0,
                loading: false
            };
        },
        computed: {
            showModal: {
                get () {
                    return this.show
                },
                set (value) {
                    this.$emit('update:show', value)
                }
            },
            // 动态设置列
            tableColumns () {
                // const columns = [...this.columns];
                // return columns.filter(item => item.show);
                return this.columns
            },
            offset () {
                return (this.current - 1) * this.limit;
            }
        },
        watch: {
            showModal (value) {
                if (!value) {
                    this.resetTable()
                } else {
                    this.getData()
                }
            }
        },
        methods: {
            hasSlot (key) {
                let data = this.columns.find(item => item.key === key)
                return data && data.slot
            },
            // 获取上报数据
            getData () {
                if (this.loading) return;
                if (!this.getList) return
                this.loading = true;
                // 下面的 params 是获取的表单查询参数
                this.getList({
                    id: this.id,
                    page: this.current, // 起始页
                    pagesize: this.limit // 查询数量
                }).then((res) => {
                    this.list = res.list;
                    this.total = res.total_count - 0;
                    this.loading = false;
                });
            },
            resetTable () {
                this.current = 1
                this.limit = 10
                this.list = []
                this.total = 0
            },
            // 切换页码
            handleChangePage (page) {
                this.current = page;
                this.getData();
            },
            // 切换每页条数
            handleChangePageSize (size) {
                this.current = 1;
                this.limit = size;
                this.getData();
            }
        },
        mounted () {
        }
    }
</script>

<style lang="less" scoped></style>
