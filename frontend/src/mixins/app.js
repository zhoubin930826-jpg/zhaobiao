/**
 * 通用混合
 * */
export default {
    methods: {
        // 当 $route 更新时触发
        appRouteChange (to, from) {

        },
        // 改变表格尺寸
        handleChangeTableSize (size) {
            this.tableSize = size;
        },
        // 表格全屏
        handleFullscreen () {
            this.tableFullscreen = !this.tableFullscreen;
            this.$emit('on-fullscreen', this.tableFullscreen);
        },
        // 刷新表格数据
        handleRefresh () {
            this.getData();
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
    }
}
