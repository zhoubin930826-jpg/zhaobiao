<template>
    <div>
        <div class="i-layout-page-header">
            <PageHeader title="管理员管理" hidden-breadcrumb />
        </div>
        <Card :bordered="false" dis-hover class="ivu-mt" ref="card">
            <table-form ref="form" @on-submit="handleSubmit" @on-reset="handleReset" />
            <table-list ref="table" @on-fullscreen="handleFullscreen" />
        </Card>
    </div>
</template>
<script>
    import screenfull from 'screenfull';
    import tableForm from './table-form';
    import tableList from './table-list';

    export default {
        name: 'system-user',
        components: { tableForm, tableList },
        data () {
            return {

            }
        },
        methods: {
            getData () {
                this.$refs.table.getData();
            },
            handleSubmit () {
                this.$refs.table.current = 1;
                this.getData();
            },
            handleReset () {
                this.$refs.table.current = 1;
                this.$refs.table.limit = 10;
                this.getData();
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
