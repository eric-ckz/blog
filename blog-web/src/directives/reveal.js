const observers = new WeakMap()

export default {
  mounted(element) {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      element.classList.add('is-revealed')
      return
    }

    element.classList.add('reveal-ready')
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry.isIntersecting) return
        element.classList.add('is-revealed')
        observer.disconnect()
      },
      { threshold: 0.12 },
    )
    observer.observe(element)
    observers.set(element, observer)
  },
  unmounted(element) {
    observers.get(element)?.disconnect()
    observers.delete(element)
  },
}
