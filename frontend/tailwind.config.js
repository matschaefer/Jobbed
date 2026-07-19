/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        // Dunkles, modernes Farbsystem (siehe Design-Referenz)
        bg: '#0a0b0f',
        surface: '#14161c',
        'surface-2': '#1b1e26',
        'surface-3': '#22262f',
        border: '#282c36',
        'border-light': '#333844',
        muted: '#8b909c',
        'text-hi': '#eef0f4',
        brand: {
          DEFAULT: '#8b7cf6',
          hover: '#7868e6',
          soft: 'rgba(139,124,246,0.15)',
          fg: '#ffffff',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      borderRadius: {
        xl: '14px',
        '2xl': '18px',
      },
      boxShadow: {
        card: '0 1px 2px rgba(0,0,0,0.4)',
        drag: '0 16px 40px rgba(0,0,0,0.55)',
        panel: '0 24px 60px rgba(0,0,0,0.5)',
      },
    },
  },
  corePlugins: {
    // Material bringt eigene Basis-Styles mit; Preflight bleibt an, stört aber kaum.
    preflight: true,
  },
  plugins: [],
};
