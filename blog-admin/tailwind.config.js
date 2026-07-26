/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#1d2433',
        mist: '#f4f6fb',
        brand: '#5d64e8',
      },
    },
  },
  plugins: [],
}
