<template>
    <div>
        <div class="i-layout-page-header">
            <PageHeader title="角色管理" hidden-breadcrumb />
        </div>
        <Card :bordered="false" dis-hover class="ivu-mt" ref="card">
            <table-list ref="table" @on-fullscreen="handleFullscreen" />
        </Card>
    </div>
</template>
<script>
    import screenfull from 'screenfull';
    import tableList from './table-list';

    export default {
        name: 'system-role',
        components: { tableList },
        methods: {
            getData () {
                this.$refs.table.getData();
            },
            handleFullscreen (state) {
                if (state) {
                    screenfull.request(this.$refs.card.$el);
                } else {
                    screenfull.exit();
                }
            }
        },
        mounted () {
            this.getData();
        }
    }
</script>
