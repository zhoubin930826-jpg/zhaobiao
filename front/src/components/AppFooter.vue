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
  border-top: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #fbfcfe 0%, #f1f5f9 100%);
  padding: 1.35rem 1.25rem 1.5rem;
  margin-top: auto;
  box-shadow: 0 -1px 0 rgba(255, 255, 255, 0.8) inset;
}

.inner {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  font-size: 0.84rem;
  color: #64748b;
}

.inner p {
  margin: 0.25rem 0;
}

.muted {
  font-size: 0.8rem;
  opacity: 0.85;
}
</style>
