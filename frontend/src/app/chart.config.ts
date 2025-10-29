import { Chart, registerables } from 'chart.js';

// Register all Chart.js components
Chart.register(...registerables);

// Global chart defaults
Chart.defaults.font.family = "'Inter', sans-serif";
Chart.defaults.color = '#4B5563'; // neutral-600
Chart.defaults.borderColor = '#E5E7EB'; // neutral-200

export { Chart };
