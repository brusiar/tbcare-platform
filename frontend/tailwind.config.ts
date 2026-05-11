import type { Config } from 'tailwindcss'

const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        // Brand colors extracted from visual reference
        brand: {
          purple: {
            DEFAULT: '#5a3c78',
            dark: '#4b3c69',
            light: '#c3b4d2',
            muted: '#8c7aa0',
          },
          warm: {
            DEFAULT: '#dca08c',
            light: '#e1b496',
            lighter: '#f0e1d2',
            cream: '#f5eae5',
          },
        },
        // Semantic aliases
        primary: {
          DEFAULT: '#5a3c78',
          hover: '#4b3c69',
          light: '#c3b4d2',
          foreground: '#ffffff',
        },
        sidebar: {
          bg: '#5a3c78',
          hover: '#4b3c69',
          active: '#3d2a5c',
          text: '#f0e1d2',
          muted: '#c3b4d2',
        },
        surface: {
          DEFAULT: '#ffffff',
          muted: '#f5eae5',
          subtle: '#f0e1d2',
        },
        text: {
          DEFAULT: '#191515',
          muted: '#6b5f5f',
          light: '#a28b8c',
        },
        accent: {
          DEFAULT: '#dca08c',
          hover: '#c88c78',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        DEFAULT: '0.5rem',
      },
    },
  },
  plugins: [],
}

export default config
