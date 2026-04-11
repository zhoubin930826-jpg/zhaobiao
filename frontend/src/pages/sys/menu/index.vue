<template>
    <div>
        <div class="i-layout-page-header" />
        <Card :bordered="false" dis-hover class="ivu-mt" ref="card">
            <table-list ref="table" @on-fullscreen="handleFullscreen" />
        </Card>
    </div>
</template>

<script>
    import screenfull from 'screenfull';
    import tableList from './table-list';

    export default {
        name: 'system-menu',
        components: { tableList },
        methods: {
            handleFullscreen (state) {
                if (state) {
                    screenfull.request(this.$refs.card.$el);
                } else {
                    screenfull.exit();
                }
            }
        }
    };
</script>
