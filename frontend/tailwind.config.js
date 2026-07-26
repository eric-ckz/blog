/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Helvetica Neue"', 'Helvetica', 'Arial', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif'],
      },
      colors: {
        paper: {
          50: '#faf8f5',
          100: '#f5f0ea',
          200: '#e8dfd3',
          300: '#d4c4b0',
          400: '#b8a08a',
          500: '#a0846a',
          600: '#8a6d54',
          700: '#6f5541',
          800: '#5a4536',
          900: '#4a392d',
        },
        ink: {
          50: '#f7f6f3',
          100: '#e3dfd6',
          200: '#c5bda9',
          300: '#a4997e',
          400: '#8b7e63',
          500: '#736651',
          600: '#5d5243',
          700: '#4b4238',
          800: '#3e3830',
          900: '#36302a',
        }
      },
    },
  },
  plugins: [],
}
