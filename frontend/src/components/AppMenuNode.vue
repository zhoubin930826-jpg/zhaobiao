<template>
  <el-sub-menu
    v-if="isGroup"
    :index="menu.code"
  >
    <template #title>
      <el-icon v-if="menu.icon">
        <component :is="menu.icon" />
      </el-icon>
      <span>{{ menu.name }}</span>
    </template>
    <app-menu-node
      v-for="child in displayChildren"
      :key="child.id"
      :menu="child"
    />
  </el-sub-menu>

  <el-menu-item
    v-else
    :index="menu.routePath"
  >
    <el-icon v-if="menu.icon">
      <component :is="menu.icon" />
    </el-icon>
    <span>{{ menu.name }}</span>
  </el-menu-item>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  menu: {
    type: Object,
    required: true
  }
});

const displayChildren = computed(() =>
  (props.menu.children || []).filter((item) => item.enabled && item.visible && item.type !== "BUTTON")
);

const isGroup = computed(() => props.menu.type === "DIRECTORY" || displayChildren.value.length > 0);
</script>

