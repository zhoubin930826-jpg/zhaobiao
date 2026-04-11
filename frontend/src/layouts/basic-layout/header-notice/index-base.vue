<template>
    <span class="i-layout-header-trigger i-layout-header-trigger-min i-layout-header-trigger-in">
    </span>
</template>
<script>
    import { mapState } from 'vuex';

    export default {
        name: 'iHeaderNotice',
        data () {
            return {
                badgeProps: {
                    offset: [20, 0]
                }
            }
        },
        computed: {
            ...mapState('admin/layout', [
                'isMobile'
            ])
        }
    }
</script>
