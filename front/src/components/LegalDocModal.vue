<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="legal-mask"
      role="presentation"
      @click.self="onClose"
    >
      <div
        class="legal-dialog"
        role="dialog"
        :aria-labelledby="titleId"
        aria-modal="true"
      >
        <header class="legal-header">
          <h2 :id="titleId" class="legal-title">{{ title }}</h2>
          <button type="button" class="legal-close" aria-label="关闭" @click="onClose">
            ×
          </button>
        </header>
        <div class="legal-body">
          <slot>
            <template v-for="(section, index) in sections" :key="index">
              <h3 v-if="section.heading" class="legal-section-title">{{ section.heading }}</h3>
              <p
                v-for="(paragraph, pIndex) in section.paragraphs"
                :key="`${index}-${pIndex}`"
                class="legal-paragraph"
              >
                {{ paragraph }}
              </p>
            </template>
          </slot>
        </div>
        <footer class="legal-footer">
          <button type="button" class="legal-confirm" @click="onClose">我已阅读</button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    required: true
  },
  /** [{ heading?: string, paragraphs: string[] }] */
  sections: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'close'])

const titleId = computed(() => `legal-doc-title-${props.title.replace(/\s/g, '-')}`)

function onClose() {
  emit('update:visible', false)
  emit('close')
}

function onKeydown(event) {
  if (event.key === 'Escape' && props.visible) {
    onClose()
  }
}

watch(
  () => props.visible,
  (open) => {
    if (typeof document === 'undefined') {
      return
    }
    if (open) {
      document.body.style.overflow = 'hidden'
      window.addEventListener('keydown', onKeydown)
    } else {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', onKeydown)
    }
  }
)

onBeforeUnmount(() => {
  if (typeof document !== 'undefined') {
    document.body.style.overflow = ''
  }
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.legal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.48);
}

.legal-dialog {
  display: flex;
  flex-direction: column;
  width: min(640px, 100%);
  max-height: min(82vh, 720px);
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.2);
  overflow: hidden;
}

.legal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.9rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
}

.legal-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--color-primary-dark, #0d3d7a);
  line-height: 1.35;
}

.legal-close {
  flex-shrink: 0;
  width: 2rem;
  height: 2rem;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #64748b;
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}

.legal-close:hover {
  background: #f1f5f9;
  color: #334155;
}

.legal-body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.15rem 1.1rem;
  font-size: 0.88rem;
  line-height: 1.65;
  color: #334155;
}

.legal-section-title {
  margin: 1rem 0 0.45rem;
  font-size: 0.92rem;
  font-weight: 600;
  color: #1e293b;
}

.legal-section-title:first-child {
  margin-top: 0;
}

.legal-paragraph {
  margin: 0 0 0.65rem;
  text-align: justify;
}

.legal-footer {
  padding: 0.75rem 1rem 1rem;
  border-top: 1px solid #e2e8f0;
  background: #fafbfd;
  text-align: center;
}

.legal-confirm {
  min-width: 7.5rem;
  padding: 0.55rem 1.25rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(180deg, #2563eb 0%, var(--color-primary, #1a5fb4) 45%, var(--color-primary-dark, #0d3d7a) 100%);
  color: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(26, 95, 180, 0.25);
  transition: filter 0.15s;
}

.legal-confirm:hover {
  filter: brightness(1.05);
}
</style>
