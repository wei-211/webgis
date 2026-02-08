<template>
  <div class="panel">
    <h3>图层管理</h3>
    <div v-for="layer in layers" :key="layer.id" class="layer-item">
      <label>
        <input
          type="checkbox"
          v-model="layer.visible"
          @change="handleChanged(layer)"
        />
        {{ layer.name }}
      </label>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { layers as config } from '@/config/layers'
import { loadLayer } from '@/api/layerApi'

const emit = defineEmits(['add', 'remove', 'toggle-function'])

const layers = reactive(
  config.map(l => ({ ...l, visible:l.active || false }))
)

function toggle(layer) {
  if (layer.visible) {
    loadLayer(layer).then(res => emit('add', layer.id, res.data))
  } else {
    emit('remove', layer.id)
  }
}

function handleChanged(layer) {
  // 2. 如果是功能类型（可达性分析）
  if (layer.type === 'function') {
    emit('toggle-function', layer)
  }
  // 3. 如果是普通的业务图层
  else {
    if (layer.visible) {
      loadLayer(layer).then(res => emit('add', layer.id, res.data))
    } else {
      emit('remove', layer.id)
    }
  }
}
</script>

<style scoped>
.panel {
  position: absolute;
  left: 10px;
  top: 10px;
  background: white;
  padding: 10px;
}
</style>
