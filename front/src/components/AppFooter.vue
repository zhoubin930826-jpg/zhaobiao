<template>
  <footer class="footer">
    <div class="inner">
      <p class="muted">© {{ year }} 招投标信息公示</p>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.footer {
  border-top: 1px solid var(--border);
  background: var(--bg-card);
  padding: 1.5rem 1.25rem;
  margin-top: auto;
}

.inner {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.inner p {
  margin: 0.25rem 0;
}

.muted {
  font-size: 0.8rem;
  opacity: 0.85;
}
</style>
