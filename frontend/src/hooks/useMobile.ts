import { ref, onMounted, onUnmounted } from 'vue'

export function useMobile() {
  const isMobile = ref(false)
  const isMobileOrTablet = ref(false)
  
  const checkMobile = () => {
    const width = window.innerWidth
    isMobile.value = width < 768
    isMobileOrTablet.value = width < 992
  }
  
  onMounted(() => {
    checkMobile()
    window.addEventListener('resize', checkMobile)
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', checkMobile)
  })
  
  return { isMobile, isMobileOrTablet }
}
