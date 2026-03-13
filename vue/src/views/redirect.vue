<template>
  <div></div>
</template>

<script>
export default {
  name: 'Redirect',
  mounted() {
    const { path, query } = this.$route.params
    if (path && query) {
      this.$router.replace({ path: path, query: JSON.parse(query) })
    } else if (path) {
      this.$router.replace({ path: path })
    }
  }
}
</script>
