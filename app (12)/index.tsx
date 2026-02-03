import React, { useState, useEffect, useRef } from 'react';
import { createRoot } from 'react-dom/client';
import { GoogleGenAI, Chat } from "@google/genai";

// --- Icons ---
const Icons = {
  MapPin: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>
  ),
  Bus: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M8 6v6"/><path d="M15 6v6"/><path d="M2 12h19.6"/><path d="M18 18h3s.5-1.7.8-2.8c.1-.4.2-.8.2-1.2 0-.4-.1-.8-.2-1.2l-1.4-5C20.1 6.8 19.1 6 18 6H4a2 2 0 0 0-2 2v10h3"/><circle cx="7" cy="18" r="2"/><path d="M9 18h5"/><circle cx="16" cy="18" r="2"/></svg>
  ),
  Leaf: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 13-1.8 17.9A7 7 0 0 1 11 20Z"/><path d="m5 9 7 7"/></svg>
  ),
  User: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
  ),
  Clock: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
  ),
  Navigation: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polygon points="3 11 22 2 13 21 11 13 3 11"/></svg>
  ),
  Award: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="8" r="7"/><polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/></svg>
  ),
  Wifi: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12.55a11 11 0 0 1 14.08 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><line x1="12" y1="20" x2="12.01" y2="20"/></svg>
  ),
  Users: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
  ),
  MessageCircle: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m3 21 1.9-5.7a8.5 8.5 0 1 1 3.8 3.8z"/></svg>
  ),
  Send: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
  ),
  Footprints: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 16v-2.38C4 11.5 2.97 10.5 3 8c.03-2.72 1.49-6 4.5-6C9.37 2 11 3.8 11 8c0 2.85-1.67 4.24-2 6.62V17c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1c0-.55-.45-1-1-1h-1"/><path d="M16 20c0-2.85 1.67-4.24 2-6.62V10c0-.55-.45-1-1-1h-1c-.55 0-1 .45-1 1v2.38c0 2.12 1.03 3.12 1 5.62-.03 2.72-1.49 6-4.5 6C10.63 22 9 20.2 9 16"/></svg>
  ),
  Calendar: () => (
     <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect width="18" height="18" x="3" y="4" rx="2" ry="2"/><line x1="16" x2="16" y1="2" y2="6"/><line x1="8" x2="8" y1="2" y2="6"/><line x1="3" x2="21" y1="10" y2="10"/></svg>
  ),
  X: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
  ),
  Plus: () => (
     <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>
  ),
  Check: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
  ),
  Settings: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
  ),
  Info: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="16" y2="12"/><line x1="12" x2="12.01" y1="8" y2="8"/></svg>
  ),
  LogOut: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/></svg>
  ),
  ChevronRight: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
  ),
  ArrowLeft: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
  ),
  Sparkles: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/></svg>
  ),
  Gift: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="8" width="18" height="4" rx="1"/><path d="M12 8v13"/><path d="M19 12v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7"/><path d="M7.5 8a2.5 2.5 0 0 1 0-5A4.8 8 0 0 1 12 8a4.9 8 0 0 1 4.5-5 2.5 2.5 0 0 1 0 5"/></svg>
  ),
  ArrowDown: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19 12 12 19 5 12"/></svg>
  ),
  Lock: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
  ),
  History: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 3v5h5"/><path d="M3.05 13A9 9 0 1 0 6 5.3L3 8"/><path d="M12 7v5l4 2"/></svg>
  )
};

// --- Mock Data ---

const ROUTES = [
  { id: 'D1', name: 'D1', from: 'Opp Hon Sui Sen', to: 'UTown', color: '#DB2777', status: 'Arriving', time: '2 min', crowd: 'Low' },
  { id: 'D2', name: 'D2', from: 'Car Park 11', to: 'UTown', color: '#7C3AED', status: 'On Time', time: '5 min', crowd: 'Med' },
  { id: 'A1', name: 'A1', from: 'PGP Terminal', to: 'Kent Ridge MRT', color: '#DC2626', status: 'Delayed', time: '12 min', crowd: 'High' },
  { id: 'A2', name: 'A2', from: 'Kent Ridge MRT', to: 'PGP Terminal', color: '#F59E0B', status: 'Arriving', time: '3 min', crowd: 'Low' },
  { id: 'BTC', name: 'BTC', from: 'Kent Ridge', to: 'Bukit Timah', color: '#059669', status: 'Scheduled', time: '25 min', crowd: 'Low' },
];

const COMMUNITIES = [
  { rank: 1, name: 'School of Computing', score: 4520, change: '+12%', color: '#3b82f6' },
  { rank: 2, name: 'Faculty of Engineering', score: 4105, change: '+8%', color: '#f97316' },
  { rank: 3, name: 'UTown Residence', score: 3850, change: '+15%', color: '#10b981' },
  { rank: 4, name: 'Business School', score: 3200, change: '+5%', color: '#eab308' },
  { rank: 5, name: 'Prince George Park', score: 2900, change: '+2%', color: '#6366f1' },
];

const SHOP_ITEMS = [
  { id: 'hat_grad', name: 'Grad Cap', type: 'head', cost: 500, icon: '🎓' },
  { id: 'hat_cap', name: 'Orange Cap', type: 'head', cost: 200, icon: '🧢' },
  { id: 'glasses_sun', name: 'Shades', type: 'face', cost: 300, icon: '🕶️' },
  { id: 'shirt_nus', name: 'NUS Tee', type: 'body', cost: 400, icon: '👕' },
  { id: 'shirt_hoodie', name: 'Blue Hoodie', type: 'body', cost: 600, icon: '🧥' },
];

const VOUCHERS = [
  { id: 'sbux_10', name: '$10 Starbucks', cost: 1000, color: '#00704A', icon: '☕', desc: 'Valid at UTown & Frontier' },
  { id: 'subway_5', name: '$5 Subway', cost: 500, color: '#FFC72C', icon: '🥪', desc: 'Valid at Yusof Ishak House' },
  { id: 'nus_2', name: '$2 Canteen Voucher', cost: 200, color: '#F97316', icon: '🍲', desc: 'Valid at The Deck & Techno' },
  { id: 'liho_3', name: '$3 LiHO Tea', cost: 350, color: '#DC2626', icon: '🧋', desc: 'Valid at Central Library' },
];

const WALKING_ROUTES = [
  { 
    id: 1, 
    title: 'Kent Ridge Forest Walk', 
    time: '15 min', 
    dist: '1.2 km', 
    calories: '85 kcal',
    tags: ['Nature', 'Hilly'],
    desc: 'A scenic route through the secondary forest connecting Science Park and Kent Ridge. Great for fresh air!',
    bg: 'linear-gradient(135deg, #a7f3d0 0%, #059669 100%)'
  },
  { 
    id: 2, 
    title: 'UTown to Science', 
    time: '10 min', 
    dist: '0.8 km', 
    calories: '50 kcal',
    tags: ['Sheltered', 'Easy'],
    desc: 'The quickest sheltered linkway avoiding the internal shuttle bus crowds. Perfect for rainy days.',
    bg: 'linear-gradient(135deg, #bfdbfe 0%, #3b82f6 100%)'
  },
  { 
    id: 3, 
    title: 'FASS Ridge Hike', 
    time: '20 min', 
    dist: '1.5 km', 
    calories: '110 kcal',
    tags: ['Workout', 'View'],
    desc: 'Challenge yourself with the stairs up the ridge. Rewards you with a panoramic view of the port.',
    bg: 'linear-gradient(135deg, #fde68a 0%, #d97706 100%)'
  }
];

const ACTIVITIES = [
  { 
    id: 1, 
    title: 'Car-Lite Friday', 
    date: 'Every Fri, 9 AM', 
    desc: 'Double points for walking!', 
    fullDesc: 'Join the campus-wide movement to reduce carbon footprint. Walk or take the shuttle bus to campus every Friday to earn 2x Green Points. Check-in at UTown Plaza.',
    icon: '🚶',
    location: 'UTown Plaza',
    category: 'Campaign',
    imageColor: '#3b82f6',
    routePlan: [
      { type: 'walk', val: '5 min', desc: 'Walk to bus stop' },
      { type: 'bus', val: 'D1', desc: 'Take D1 to UTown' },
      { type: 'walk', val: '2 min', desc: 'Walk to Plaza' }
    ]
  },
  { 
    id: 2, 
    title: 'BYO Container Day', 
    date: 'Until 30th Sep', 
    desc: 'Scan QR at canteens.', 
    fullDesc: 'Bring your own reusable container to any campus canteen (The Deck, Frontier, Techno) and get $0.50 off your meal plus 50 bonus points!',
    icon: '🍱',
    location: 'The Deck',
    category: 'Food',
    imageColor: '#f97316',
    routePlan: [
      { type: 'walk', val: '8 min', desc: 'Walk via Ridge Path' },
      { type: 'bus', val: 'A1', desc: 'Take A1 to Central Lib' },
      { type: 'walk', val: '3 min', desc: 'Walk to The Deck' }
    ]
  },
  { 
    id: 3, 
    title: 'Ridge Run 2024', 
    date: 'Sat, 12 Oct', 
    desc: 'Annual charity run.', 
    fullDesc: 'The annual NUS Ridge Run is back! 10km competitive and 5km fun run through the scenic Kent Ridge terrain. Register now to get a limited edition tee.',
    icon: '🏃',
    location: 'Sports Hall',
    category: 'Sports',
    imageColor: '#ef4444',
    routePlan: [
      { type: 'walk', val: '10 min', desc: 'Warm up walk' },
      { type: 'bus', val: 'K', desc: 'Take K to MPSH' }
    ]
  },
  { 
    id: 4, 
    title: 'Plant-Based Workshop', 
    date: 'Wed, 16 Oct', 
    desc: 'Learn to cook green.', 
    fullDesc: 'Discover the art of sustainable cooking. Chef Li will demonstrate 3 easy plant-based recipes perfect for dorm living.',
    icon: '🥗',
    location: 'PGP Function Room',
    category: 'Food',
    imageColor: '#10b981',
    routePlan: [
      { type: 'walk', val: '2 min', desc: 'Walk to stop' },
      { type: 'bus', val: 'A2', desc: 'Take A2 to PGP' },
      { type: 'walk', val: '5 min', desc: 'Walk to block' }
    ]
  },
];

const ACHIEVEMENTS = [
  { id: '1', name: 'Freshman', icon: '🌱', desc: 'First eco-trip completed', unlocked: true },
  { id: '2', name: 'Bus Pro', icon: '🚌', desc: '10 Shuttle rides', unlocked: true },
  { id: '3', name: 'Hiker', icon: '🥾', desc: 'Walked > 5km', unlocked: false },
  { id: '4', name: 'Carbon Zero', icon: '♻️', desc: 'Saved 10kg CO2', unlocked: false },
  { id: '5', name: 'Early Bird', icon: '🌅', desc: 'Ride before 8AM', unlocked: true },
  { id: '6', name: 'Night Owl', icon: '🦉', desc: 'Ride after 10PM', unlocked: false },
];

const HISTORY = [
  { id: 1, action: 'Bus D1 to UTown', time: '10:30 AM', points: '+50', type: 'earn' },
  { id: 2, action: 'Walked to Library', time: 'Yesterday', points: '+30', type: 'earn' },
  { id: 3, action: 'Bought Orange Cap', time: 'Yesterday', points: '-200', type: 'spend' },
  { id: 4, action: 'Daily Check-in', time: 'Yesterday', points: '+10', type: 'earn' },
  { id: 5, action: 'Bus A2 to PGP', time: '2 days ago', points: '+50', type: 'earn' },
];

// --- Styles ---

const theme = {
  primary: '#15803d', // Darker Green from Image (Emerald 700)
  primaryHover: '#14532d',
  secondary: '#F97316', // Orange 500
  bg: '#F0FDF4', // Mint 50
  surface: '#FFFFFF',
  text: '#1e293b',
  subtext: '#64748b',
  border: '#e2e8f0',
  error: '#EF4444',
};

const mobileStyles = {
  phoneContainer: {
    width: '375px',
    height: '812px',
    backgroundColor: theme.bg,
    borderRadius: '40px',
    boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 0 12px #1e293b',
    overflow: 'hidden',
    position: 'relative' as const,
    display: 'flex',
    flexDirection: 'column' as const,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
  statusBar: {
    height: '44px',
    width: '100%',
    display: 'flex',
    justifyContent: 'space-between',
    padding: '0 24px',
    alignItems: 'center',
    fontSize: '12px',
    fontWeight: 600,
    color: '#000',
    backgroundColor: 'rgba(255,255,255,0.8)',
    backdropFilter: 'blur(5px)',
    zIndex: 50,
    position: 'absolute' as const,
    top: 0,
  },
  notch: {
    position: 'absolute' as const,
    top: '0',
    left: '50%',
    transform: 'translateX(-50%)',
    width: '150px',
    height: '30px',
    backgroundColor: '#1e293b',
    borderBottomLeftRadius: '16px',
    borderBottomRightRadius: '16px',
    zIndex: 60,
  },
  content: {
    flex: 1,
    overflowY: 'auto' as const,
    padding: '60px 20px 100px 20px', // Top padding for status bar, bottom for nav
  },
  bottomNav: {
    position: 'absolute' as const,
    bottom: 0,
    width: '100%',
    height: '85px',
    backgroundColor: theme.surface,
    borderTop: `1px solid ${theme.border}`,
    display: 'flex',
    justifyContent: 'space-around',
    paddingTop: '16px',
    zIndex: 50,
  },
  navItem: {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    fontSize: '9px',
    gap: '4px',
    cursor: 'pointer',
    color: theme.subtext,
    transition: 'color 0.2s',
    width: '50px',
  },
  navItemActive: {
    color: theme.primary,
  }
};

// --- Animations CSS ---
const animationStyles = `
  @keyframes breathe {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.02); }
  }
  @keyframes blink {
    0%, 48%, 52%, 100% { transform: scaleY(1); }
    50% { transform: scaleY(0.1); }
  }
  @keyframes jump {
    0% { transform: translateY(0); }
    50% { transform: translateY(-10px); }
    100% { transform: translateY(0); }
  }
  @keyframes wave {
    0% { transform: rotate(0deg); }
    25% { transform: rotate(-10deg); }
    75% { transform: rotate(10deg); }
    100% { transform: rotate(0deg); }
  }
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  @keyframes popIn {
    0% { transform: scale(0.8); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
  }
`;


// --- Extracted UI Components ---

// 1. Styled Button
const StyledButton = ({ onClick, disabled, children, variant = 'primary', style = {} }: any) => (
  <button
    onClick={onClick}
    disabled={disabled}
    style={{
      padding: '16px',
      borderRadius: '9999px', // Pill shape
      border: variant === 'outline' ? `2px solid ${theme.primary}` : 'none',
      background: disabled ? '#9CA3AF' : (variant === 'outline' ? 'transparent' : theme.primary),
      color: variant === 'outline' ? theme.primary : 'white',
      fontSize: '16px',
      fontWeight: 'bold',
      cursor: disabled ? 'not-allowed' : 'pointer',
      width: '100%',
      boxShadow: disabled || variant === 'outline' ? 'none' : '0 4px 6px -1px rgba(21, 128, 61, 0.4)',
      transition: 'all 0.2s',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      ...style
    }}
    onMouseDown={(e) => !disabled && variant !== 'outline' && (e.currentTarget.style.background = theme.primaryHover)}
    onMouseUp={(e) => !disabled && variant !== 'outline' && (e.currentTarget.style.background = theme.primary)}
    onMouseLeave={(e) => !disabled && variant !== 'outline' && (e.currentTarget.style.background = theme.primary)}
  >
    {children}
  </button>
);

// 2. Styled Input
const StyledInput = ({ label, error, ...props }: any) => (
  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', width: '100%' }}>
    {label && <label style={{ fontSize: '12px', fontWeight: 600, color: theme.primary, marginLeft: '8px' }}>{label}</label>}
    <input
      {...props}
      style={{
        padding: '16px',
        borderRadius: '12px',
        border: error ? `2px solid ${theme.error}` : `2px solid ${theme.border}`,
        outline: 'none',
        fontSize: '14px',
        transition: 'border-color 0.2s',
        width: '100%',
        background: 'white'
      }}
      onFocus={(e) => !error && (e.target.style.borderColor = theme.primary)}
      onBlur={(e) => !error && (e.target.style.borderColor = theme.border)}
    />
    {error && <span style={{ color: theme.error, fontSize: '11px', marginLeft: '8px' }}>{error}</span>}
  </div>
);

// 3. Styled Toggle
const StyledToggle = ({ active, onClick }: any) => (
  <div 
    onClick={onClick}
    style={{ 
      width: '48px', height: '28px', borderRadius: '14px', 
      background: active ? theme.primary : '#e2e8f0', 
      position: 'relative', cursor: 'pointer', transition: 'background 0.2s'
    }}
  >
    <div style={{ 
      width: '24px', height: '24px', borderRadius: '50%', background: 'white',
      position: 'absolute', top: '2px', left: active ? '22px' : '2px', 
      transition: 'left 0.2s', boxShadow: '0 1px 2px rgba(0,0,0,0.2)'
    }} />
  </div>
);

// 4. Success Modal
const SuccessModal = ({ isOpen, onClose, message, points }: any) => {
  if (!isOpen) return null;
  return (
    <div style={{
      position: 'absolute', inset: 0, zIndex: 200,
      background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center',
      backdropFilter: 'blur(2px)'
    }}>
      <div style={{
        background: 'white', padding: '32px', borderRadius: '24px',
        width: '80%', maxWidth: '300px',
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
        animation: 'popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
      }}>
        <div style={{ width: '100px', height: '100px', marginBottom: '16px' }}>
           <MascotLion outfit={{ head: 'none', face: 'none', body: 'shirt_nus' }} />
        </div>
        <h2 style={{ fontSize: '24px', color: theme.primary, margin: '0 0 8px 0', fontWeight: 800 }}>Success!</h2>
        <p style={{ margin: '0 0 24px 0', textAlign: 'center', color: theme.subtext, fontSize: '14px' }}>
          {message}
          {points && <span style={{ display: 'block', fontWeight: 'bold', color: theme.text, marginTop: '4px' }}>{points}</span>}
        </p>
        <StyledButton onClick={onClose} style={{ width: '100px', padding: '10px' }}>OK</StyledButton>
      </div>
    </div>
  );
};

// 5. Custom Toast
const CustomToast = ({ message }: { message: string }) => (
  <div style={{ 
    position: 'absolute', bottom: '100px', left: '50%', transform: 'translateX(-50%)',
    background: theme.primary, color: 'white', padding: '12px 24px', 
    borderRadius: '999px', fontSize: '14px', zIndex: 150,
    display: 'flex', alignItems: 'center', gap: '8px',
    boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)',
    animation: 'slideUp 0.3s ease-out',
    fontWeight: 600
  }}>
    <div style={{ background: 'rgba(255,255,255,0.2)', borderRadius: '50%', padding: '2px' }}><Icons.Check /></div>
    {message}
  </div>
);

// 6. Spinner
const Spinner = () => (
  <div style={{
    width: '24px', height: '24px', border: `3px solid ${theme.bg}`,
    borderTop: `3px solid ${theme.primary}`, borderRadius: '50%',
    animation: 'spin 1s linear infinite'
  }}></div>
);


// --- Standard Components ---

const StatusBadge = ({ status }: { status: string }) => {
  let bg = '#E0F2FE';
  let text = '#0369A1';
  
  if (status === 'Arriving') { bg = '#DCFCE7'; text = '#15803D'; }
  else if (status === 'Delayed') { bg = '#FEE2E2'; text = '#B91C1C'; }
  else if (status === 'Crowded') { bg = '#FEF3C7'; text = '#B45309'; }

  return (
    <span style={{ 
      backgroundColor: bg, 
      color: text, 
      padding: '2px 8px', 
      borderRadius: '12px', 
      fontSize: '10px', 
      fontWeight: 700 
    }}>
      {status}
    </span>
  );
};

// --- Campus Map Components ---

const FacultyPin = ({ x, y, color, icon, label, onClick, showLabel = true, interactive = true }: any) => (
  <g transform={`translate(${x},${y})`} onClick={interactive ? onClick : undefined} style={{ cursor: interactive ? 'pointer' : 'default' }}>
    {/* Pin Body */}
    <circle cx="0" cy="0" r="18" fill="white" stroke={color} strokeWidth="2" filter="drop-shadow(0px 2px 2px rgba(0,0,0,0.1))" />
    <text x="0" y="5" textAnchor="middle" fontSize="16">{icon}</text>
    {/* Label */}
    {showLabel && (
      <rect x="-30" y="24" width="60" height="16" rx="8" fill={color} />
    )}
    {showLabel && (
      <text x="0" y="35" textAnchor="middle" fontSize="10" fontWeight="bold" fill="white">{label}</text>
    )}
  </g>
);

const CartoonMap = ({ simplified = false, onPinClick }: { simplified?: boolean, onPinClick?: (faculty: any) => void }) => {
  const faculties = [
    { id: 'utown', name: 'UTown', x: 280, y: 80, color: '#10b981', icon: '🏙️', score: 3850, rank: 3 },
    { id: 'engin', name: 'Engin', x: 80, y: 150, color: '#f97316', icon: '⚙️', score: 4105, rank: 2 },
    { id: 'soc', name: 'SoC', x: 220, y: 180, color: '#3b82f6', icon: '💻', score: 4520, rank: 1 },
    { id: 'science', name: 'Science', x: 160, y: 280, color: '#8b5cf6', icon: '🧪', score: 2800, rank: 6 },
    { id: 'fass', name: 'FASS', x: 60, y: 350, color: '#eab308', icon: '📚', score: 3100, rank: 4 },
    { id: 'biz', name: 'Biz', x: 250, y: 400, color: '#ec4899', icon: '📈', score: 3200, rank: 4 },
  ];

  return (
    <svg width="100%" height="100%" viewBox="0 0 375 600" preserveAspectRatio="xMidYMid slice">
      <defs>
        <pattern id="grassPattern" patternUnits="userSpaceOnUse" width="100" height="100">
           <rect width="100" height="100" fill="#dcfce7" />
           <circle cx="20" cy="20" r="2" fill="#bbf7d0" />
           <circle cx="70" cy="70" r="2" fill="#bbf7d0" />
        </pattern>
      </defs>
      
      {/* Background */}
      <rect width="375" height="600" fill="url(#grassPattern)" />
      
      {/* Cartoon Roads */}
      <path 
        d="M -20 100 Q 100 100 120 180 T 160 300 T 260 400 T 400 450" 
        stroke="white" strokeWidth="25" fill="none" strokeLinecap="round" 
      />
      <path 
        d="M 280 -20 Q 280 100 220 180" 
        stroke="white" strokeWidth="20" fill="none" strokeLinecap="round" 
      />
       <path 
        d="M 120 180 Q 40 200 40 350" 
        stroke="white" strokeWidth="15" fill="none" strokeLinecap="round" 
      />

      {/* Faculty Pins */}
      {faculties.map(f => (
        <FacultyPin 
          key={f.id} 
          {...f} 
          showLabel={!simplified}
          interactive={!simplified}
          onClick={() => onPinClick && onPinClick(f)} 
        />
      ))}

      {/* Decorative Trees */}
      <circle cx="40" cy="40" r="15" fill="#4ade80" opacity="0.6" />
      <circle cx="340" cy="200" r="20" fill="#4ade80" opacity="0.6" />
      <circle cx="300" cy="550" r="25" fill="#4ade80" opacity="0.6" />
    </svg>
  );
};

// --- Auth & Onboarding Components ---

const LoginScreen = ({ onLogin, onSignUp }: { onLogin: () => void, onSignUp: () => void }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  return (
    <div style={{ 
      height: '100%', display: 'flex', flexDirection: 'column', 
      justifyContent: 'center', padding: '32px', 
      background: `linear-gradient(180deg, ${theme.bg} 0%, #dcfce7 100%)`
    }}>
      <div style={{ textAlign: 'center', marginBottom: '40px' }}>
         <div style={{ 
           width: '100px', height: '100px', margin: '0 auto 20px', 
           background: 'white', borderRadius: '50%', 
           display: 'flex', alignItems: 'center', justifyContent: 'center',
           fontSize: '50px', boxShadow: '0 10px 25px -5px rgba(5, 150, 105, 0.3)'
         }}>🦁</div>
         <h1 style={{ fontSize: '28px', color: theme.primary, marginBottom: '8px', fontWeight: 800 }}>NUS EcoRide</h1>
         <p style={{ color: theme.subtext, fontSize: '14px' }}>Green commuting for a better campus.</p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <StyledInput 
          label="NUSNET ID"
          type="text" 
          placeholder="e.g. e0123456" 
          value={email}
          onChange={(e: any) => setEmail(e.target.value)}
        />
        <StyledInput 
          label="Password"
          type="password" 
          placeholder="Enter your password" 
          value={password}
          onChange={(e: any) => setPassword(e.target.value)}
        />
        <div style={{ marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <StyledButton onClick={onLogin}>Sign In</StyledButton>
          <StyledButton variant="outline" onClick={onSignUp}>Create Account</StyledButton>
        </div>
      </div>
      
      <p style={{ textAlign: 'center', marginTop: '32px', fontSize: '12px', color: theme.subtext }}>
        By signing in, you agree to our Terms & Privacy Policy.
      </p>
    </div>
  );
};

const Onboarding = ({ onComplete }: { onComplete: () => void }) => {
  const [step, setStep] = useState(0);

  const steps = [
    { icon: <Icons.Bus />, title: "Track Rides", desc: "Real-time updates for all campus shuttle buses." },
    { icon: <Icons.Leaf />, title: "Reduce CO2", desc: "Choose green routes and track your carbon savings." },
    { icon: <Icons.Award />, title: "Earn Rewards", desc: "Collect points and dress up your LiNUS mascot!" },
  ];

  const handleNext = () => {
    if (step < steps.length - 1) {
      setStep(step + 1);
    } else {
      onComplete();
    }
  };

  return (
    <div style={{ 
      height: '100%', display: 'flex', flexDirection: 'column', 
      alignItems: 'center', justifyContent: 'center', padding: '32px',
      background: 'white', position: 'relative'
    }}>
      {/* Progress Dots */}
      <div style={{ position: 'absolute', top: '40px', display: 'flex', gap: '8px' }}>
        {steps.map((_, i) => (
          <div key={i} style={{ 
            width: '8px', height: '8px', borderRadius: '50%', 
            background: i === step ? theme.primary : '#e2e8f0',
            transition: 'background 0.3s'
          }} />
        ))}
      </div>

      <div style={{ 
        width: '120px', height: '120px', borderRadius: '50%', 
        background: '#ecfdf5', color: theme.primary,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        marginBottom: '32px'
      }}>
        <div style={{ transform: 'scale(2.5)' }}>{steps[step].icon}</div>
      </div>

      <h2 style={{ fontSize: '24px', marginBottom: '16px', textAlign: 'center', color: theme.primary, fontWeight: 700 }}>{steps[step].title}</h2>
      <p style={{ fontSize: '14px', color: theme.subtext, textAlign: 'center', lineHeight: '1.5', maxWidth: '240px' }}>
        {steps[step].desc}
      </p>

      <div style={{ marginTop: '60px', width: '100%' }}>
        <StyledButton onClick={handleNext}>
          {step === steps.length - 1 ? 'Get Started' : 'Next'} <Icons.ChevronRight />
        </StyledButton>
      </div>
    </div>
  );
};

// --- Settings & About Screens ---

const SettingsScreen = ({ onBack, onLogout }: { onBack: () => void, onLogout: () => void }) => {
  const [notif, setNotif] = useState(true);
  const [dark, setDark] = useState(false);

  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '24px 20px', background: 'white', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}` }}>
        <div onClick={onBack} style={{ cursor: 'pointer' }}><Icons.ArrowLeft /></div>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>Settings</h2>
      </div>

      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <div style={{ background: 'white', borderRadius: '16px', padding: '0 16px' }}>
          <div style={{ padding: '16px 0', borderBottom: `1px solid ${theme.border}`, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px' }}>Push Notifications</span>
            <StyledToggle active={notif} onClick={() => setNotif(!notif)} />
          </div>
          <div style={{ padding: '16px 0', borderBottom: `1px solid ${theme.border}`, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px' }}>Dark Mode</span>
            <StyledToggle active={dark} onClick={() => setDark(!dark)} />
          </div>
          <div style={{ padding: '16px 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px' }}>Location Services</span>
            <span style={{ fontSize: '12px', color: theme.subtext }}>While Using</span>
          </div>
        </div>

        <div style={{ background: 'white', borderRadius: '16px', padding: '0 16px' }}>
          <div style={{ padding: '16px 0', borderBottom: `1px solid ${theme.border}`, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px' }}>Edit Profile</span>
            <Icons.ChevronRight />
          </div>
          <div style={{ padding: '16px 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '14px' }}>Change Password</span>
            <Icons.ChevronRight />
          </div>
        </div>

        <button 
          onClick={onLogout}
          style={{ 
            marginTop: '20px', width: '100%', padding: '16px', borderRadius: '999px', 
            background: '#fee2e2', color: '#ef4444', border: 'none', 
            fontWeight: 'bold', fontSize: '14px', cursor: 'pointer',
            transition: 'background 0.2s'
          }}
        >
          Sign Out
        </button>
      </div>
    </div>
  );
};

const AboutScreen = ({ onBack }: { onBack: () => void }) => {
  return (
    <div style={{ height: '100%', background: 'white', display: 'flex', flexDirection: 'column' }}>
       <div style={{ padding: '24px 20px', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}` }}>
        <div onClick={onBack} style={{ cursor: 'pointer' }}><Icons.ArrowLeft /></div>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>About Us</h2>
      </div>
      <div style={{ padding: '32px', textAlign: 'center' }}>
        <div style={{ fontSize: '48px', marginBottom: '16px' }}>🦁</div>
        <h3 style={{ fontSize: '20px', marginBottom: '8px', color: theme.primary, fontWeight: 700 }}>NUS EcoRide</h3>
        <p style={{ color: theme.subtext, fontSize: '14px', marginBottom: '32px' }}>Version 1.2.0</p>
        
        <p style={{ fontSize: '14px', lineHeight: '1.6', color: '#334155', marginBottom: '20px' }}>
          Our mission is to transform campus commuting into a sustainable, community-driven experience. Every ride counts towards a greener NUS.
        </p>

        <div style={{ textAlign: 'left', marginTop: '40px' }}>
          <h4 style={{ fontSize: '14px', marginBottom: '12px', color: theme.text, fontWeight: 600 }}>Credits</h4>
          <p style={{ fontSize: '13px', color: theme.subtext }}>Developed by the NUS Green Team.</p>
          <p style={{ fontSize: '13px', color: theme.subtext, marginTop: '4px' }}>Bus data provided by NUS NextBus API.</p>
        </div>
      </div>
    </div>
  );
};

// --- Map Screen ---

const MapScreen = ({ onBack }: { onBack: () => void }) => {
  const [selectedFaculty, setSelectedFaculty] = useState<any>(null);

  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column', position: 'relative' }}>
      {/* Header */}
      <div style={{ 
        padding: '24px 20px', background: 'white', display: 'flex', 
        alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}`,
        position: 'absolute', top: 0, left: 0, right: 0, zIndex: 10
      }}>
        <div onClick={onBack} style={{ cursor: 'pointer', padding: '4px' }}><Icons.ArrowLeft /></div>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>Campus Map</h2>
      </div>
      
      {/* Map */}
      <div style={{ flex: 1, overflow: 'hidden', position: 'relative' }}>
        <CartoonMap onPinClick={setSelectedFaculty} />
      </div>

      {/* Pop Up */}
      {selectedFaculty && (
        <div style={{ 
          position: 'absolute', bottom: '20px', left: '20px', right: '20px', 
          background: 'white', padding: '16px', borderRadius: '16px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)', animation: 'slideUp 0.3s ease-out'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div style={{ display: 'flex', gap: '12px' }}>
               <div style={{ width: '40px', height: '40px', borderRadius: '12px', background: `${selectedFaculty.color}20`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '24px' }}>
                  {selectedFaculty.icon}
               </div>
               <div>
                  <h3 style={{ margin: 0, fontSize: '16px' }}>{selectedFaculty.name}</h3>
                  <div style={{ fontSize: '12px', color: theme.subtext }}>Current Rank: #{selectedFaculty.rank}</div>
               </div>
            </div>
            <div onClick={() => setSelectedFaculty(null)} style={{ cursor: 'pointer' }}><Icons.X /></div>
          </div>
          <div style={{ marginTop: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div style={{ flex: 1, height: '8px', background: '#e2e8f0', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{ width: '80%', height: '100%', background: selectedFaculty.color }}></div>
            </div>
            <span style={{ fontSize: '12px', fontWeight: 'bold' }}>{selectedFaculty.score} pts</span>
          </div>
        </div>
      )}
    </div>
  );
};

// --- Voucher Screen ---

const VoucherScreen = ({ onBack, points, setPoints }: { onBack: () => void, points: number, setPoints: (p: number) => void }) => {
  const [successModal, setSuccessModal] = useState<{open: boolean, msg: string, points: string | null}>({ open: false, msg: '', points: null });

  const handleRedeem = (voucher: any) => {
    if (points >= voucher.cost) {
      setPoints(points - voucher.cost);
      setSuccessModal({ open: true, msg: `Redeemed ${voucher.name}`, points: `-${voucher.cost} pts` });
    } else {
      // Just for demo, show success modal style error or simple alert, sticking to request
      alert('Not enough points!'); 
    }
  };

  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column' }}>
       {/* Header */}
       <div style={{ padding: '24px 20px', background: 'white', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}` }}>
        <div onClick={onBack} style={{ cursor: 'pointer' }}><Icons.ArrowLeft /></div>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>Redeem Rewards</h2>
      </div>

      {/* Content */}
      <div style={{ padding: '20px', overflowY: 'auto' }}>
         <div style={{ 
           background: `linear-gradient(135deg, ${theme.secondary} 0%, #EA580C 100%)`, 
           borderRadius: '16px', padding: '20px', color: 'white', marginBottom: '24px',
           display: 'flex', justifyContent: 'space-between', alignItems: 'center',
           boxShadow: '0 8px 16px -4px rgba(249, 115, 22, 0.4)'
         }}>
            <div>
              <div style={{ fontSize: '12px', opacity: 0.9 }}>CURRENT BALANCE</div>
              <div style={{ fontSize: '28px', fontWeight: 'bold' }}>{points} <span style={{fontSize:'16px'}}>pts</span></div>
            </div>
            <div style={{ fontSize: '32px' }}>🎁</div>
         </div>

         <h3 style={{ fontSize: '16px', marginBottom: '12px', fontWeight: 700, color: theme.text }}>Available Vouchers</h3>
         <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {VOUCHERS.map(v => (
              <div key={v.id} style={{ 
                background: 'white', borderRadius: '16px', padding: '16px',
                display: 'flex', alignItems: 'center', gap: '16px',
                opacity: points >= v.cost ? 1 : 0.6
              }}>
                 <div style={{ 
                   width: '48px', height: '48px', borderRadius: '12px', 
                   background: v.color, color: 'white', fontSize: '24px',
                   display: 'flex', alignItems: 'center', justifyContent: 'center'
                 }}>
                   {v.icon}
                 </div>
                 <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: '14px' }}>{v.name}</div>
                    <div style={{ fontSize: '11px', color: theme.subtext }}>{v.desc}</div>
                 </div>
                 <button 
                   onClick={() => handleRedeem(v)}
                   disabled={points < v.cost}
                   style={{ 
                     background: points >= v.cost ? theme.primary : '#e2e8f0', 
                     color: points >= v.cost ? 'white' : theme.subtext, 
                     border: 'none', padding: '8px 16px', borderRadius: '20px',
                     fontSize: '12px', fontWeight: 600, cursor: points >= v.cost ? 'pointer' : 'default'
                   }}
                 >
                   {v.cost} pts
                 </button>
              </div>
            ))}
         </div>
      </div>

      <SuccessModal 
        isOpen={successModal.open} 
        onClose={() => setSuccessModal({ ...successModal, open: false })} 
        message={successModal.msg}
        points={successModal.points}
      />
    </div>
  );
};


// --- Activity Screen & Eco Route Logic ---

const ActivityRouteView = ({ route, onClose }: { route: any[], onClose: () => void }) => {
  return (
    <div style={{ 
      position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.5)', 
      zIndex: 100, display: 'flex', alignItems: 'flex-end'
    }}>
      <div style={{ 
        width: '100%', background: 'white', borderRadius: '24px 24px 0 0', 
        padding: '24px', animation: 'slideUp 0.3s ease-out',
        maxHeight: '80vh', display: 'flex', flexDirection: 'column'
      }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <div>
            <div style={{ fontSize: '12px', color: theme.primary, fontWeight: 700, textTransform: 'uppercase' }}>Eco-Itinerary</div>
            <h2 style={{ margin: 0, fontSize: '20px' }}>Your Green Path</h2>
          </div>
          <div onClick={onClose} style={{ padding: '4px', cursor: 'pointer' }}><Icons.X /></div>
        </div>

        {/* Route Timeline */}
        <div style={{ paddingLeft: '8px', borderLeft: '2px dashed #cbd5e1', marginLeft: '12px', display: 'flex', flexDirection: 'column', gap: '32px', marginBottom: '32px' }}>
          {route.map((step, index) => (
             <div key={index} style={{ position: 'relative', paddingLeft: '24px' }}>
               {/* Node Dot */}
               <div style={{ 
                 position: 'absolute', left: '-15px', top: '0', width: '28px', height: '28px', 
                 borderRadius: '50%', background: theme.bg, border: `2px solid ${theme.primary}`,
                 display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px',
                 zIndex: 2
               }}>
                  {step.type === 'walk' ? '🚶' : step.type === 'bus' ? '🚌' : '🏁'}
               </div>
               
               {/* Content */}
               <div>
                  <div style={{ fontWeight: 600, fontSize: '16px', color: theme.text }}>{step.desc}</div>
                  <div style={{ fontSize: '13px', color: theme.subtext, marginTop: '2px' }}>
                    {step.val} {step.type === 'walk' ? '• 25 kcal' : '• Carbon-Lite'}
                  </div>
               </div>
             </div>
          ))}
          
           <div style={{ position: 'relative', paddingLeft: '24px' }}>
              <div style={{ 
                 position: 'absolute', left: '-15px', top: '0', width: '28px', height: '28px', 
                 borderRadius: '50%', background: theme.primary, color: 'white',
                 display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '14px',
                 zIndex: 2
               }}>
                  🏁
               </div>
               <div>
                  <div style={{ fontWeight: 600, fontSize: '16px', color: theme.text }}>Arrive at Venue</div>
                  <div style={{ fontSize: '13px', color: theme.primary, fontWeight: 700, marginTop: '2px' }}>+50 Bonus Points</div>
               </div>
           </div>
        </div>

        {/* Stats */}
        <div style={{ background: '#ecfdf5', borderRadius: '16px', padding: '16px', marginBottom: '24px', display: 'flex', gap: '16px', alignItems: 'center' }}>
           <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
             <Icons.Leaf />
           </div>
           <div>
             <div style={{ fontWeight: 700, fontSize: '14px', color: '#166534' }}>Total CO2 Saved: 240g</div>
             <div style={{ fontSize: '12px', color: '#15803d' }}>Compared to taking a Grab/Gojek</div>
           </div>
        </div>

        <StyledButton onClick={onClose}><Icons.Navigation /> Start Navigation</StyledButton>
      </div>
    </div>
  );
};

const ActivityDetailModal = ({ activity, onClose }: { activity: any, onClose: () => void }) => {
  const [showRoute, setShowRoute] = useState(false);

  if (showRoute) return <ActivityRouteView route={activity.routePlan} onClose={onClose} />;

  return (
    <div style={{ 
      position: 'absolute', inset: 0, background: 'white', 
      zIndex: 50, display: 'flex', flexDirection: 'column',
      animation: 'slideUp 0.3s ease-out'
    }}>
      {/* Cover Image Placeholder */}
      <div style={{ height: '240px', background: activity.imageColor, position: 'relative' }}>
         <div onClick={onClose} style={{ position: 'absolute', top: '48px', left: '20px', background: 'rgba(0,0,0,0.3)', width: '36px', height: '36px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', cursor: 'pointer', backdropFilter: 'blur(4px)' }}>
           <Icons.ArrowLeft />
         </div>
         <div style={{ position: 'absolute', bottom: '-24px', right: '24px', width: '56px', height: '56px', background: 'white', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '32px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
           {activity.icon}
         </div>
      </div>

      <div style={{ padding: '32px 24px 24px', flex: 1, overflowY: 'auto' }}>
         <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
            <span style={{ fontSize: '12px', background: '#f1f5f9', padding: '4px 10px', borderRadius: '6px', fontWeight: 600, color: theme.subtext }}>{activity.category}</span>
         </div>
         <h1 style={{ fontSize: '28px', margin: '0 0 8px 0', lineHeight: 1.2 }}>{activity.title}</h1>
         
         <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', color: theme.text }}>
               <div style={{ color: theme.subtext }}><Icons.Calendar /></div>
               <span style={{ fontSize: '14px', fontWeight: 500 }}>{activity.date}</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', color: theme.text }}>
               <div style={{ color: theme.subtext }}><Icons.MapPin /></div>
               <span style={{ fontSize: '14px', fontWeight: 500 }}>{activity.location}</span>
            </div>
         </div>

         <div style={{ margin: '32px 0', height: '1px', background: theme.border }}></div>

         <h3 style={{ fontSize: '18px', margin: '0 0 12px 0' }}>About Event</h3>
         <p style={{ fontSize: '15px', lineHeight: '1.6', color: '#334155', margin: 0 }}>
            {activity.fullDesc}
         </p>
      </div>

      <div style={{ padding: '24px', borderTop: `1px solid ${theme.border}` }}>
         <StyledButton onClick={() => setShowRoute(true)}><Icons.MapPin /> Plan Eco-Route</StyledButton>
      </div>
    </div>
  );
};

const ActivityScreen = ({ onBack }: { onBack: () => void }) => {
  const [filter, setFilter] = useState('All');
  const [selectedActivity, setSelectedActivity] = useState<any>(null);

  const filteredActivities = filter === 'All' ? ACTIVITIES : ACTIVITIES.filter(a => a.category === filter);

  if (selectedActivity) {
    return <ActivityDetailModal activity={selectedActivity} onClose={() => setSelectedActivity(null)} />;
  }

  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <div style={{ padding: '24px 20px', background: 'white', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}` }}>
        <div onClick={onBack} style={{ cursor: 'pointer' }}><Icons.ArrowLeft /></div>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>Campus Events</h2>
      </div>

      {/* Filters */}
      <div style={{ padding: '16px 20px 8px', display: 'flex', gap: '8px', overflowX: 'auto' }}>
        {['All', 'Campaign', 'Food', 'Sports'].map(f => (
          <button 
            key={f}
            onClick={() => setFilter(f)}
            style={{ 
              padding: '8px 16px', borderRadius: '20px', border: 'none', 
              background: filter === f ? theme.text : 'white', 
              color: filter === f ? 'white' : theme.text, 
              fontSize: '13px', fontWeight: 600, flexShrink: 0,
              boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
            }}
          >
            {f}
          </button>
        ))}
      </div>

      {/* Feed */}
      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px', overflowY: 'auto' }}>
        {filteredActivities.map(act => (
          <div 
            key={act.id}
            onClick={() => setSelectedActivity(act)}
            style={{ 
              background: 'white', borderRadius: '20px', overflow: 'hidden',
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)', cursor: 'pointer',
              display: 'flex', flexDirection: 'column'
            }}
          >
             <div style={{ height: '100px', background: act.imageColor, position: 'relative' }}>
                <div style={{ position: 'absolute', top: '12px', right: '12px', background: 'rgba(255,255,255,0.9)', padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 700, color: theme.text }}>
                   {act.category}
                </div>
             </div>
             <div style={{ padding: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                   <div>
                      <div style={{ fontSize: '12px', color: theme.primary, fontWeight: 600, marginBottom: '4px' }}>{act.date}</div>
                      <h3 style={{ margin: 0, fontSize: '18px' }}>{act.title}</h3>
                   </div>
                   <div style={{ fontSize: '24px' }}>{act.icon}</div>
                </div>
                <div style={{ marginTop: '12px', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: theme.subtext }}>
                   <Icons.MapPin /> {act.location}
                </div>
             </div>
          </div>
        ))}
      </div>
    </div>
  );
};

// --- Main Screens (Home, etc.) ---

const HomeScreen = ({ onOpenMap, onOpenActivities, onOpenProfile }: { onOpenMap: () => void, onOpenActivities: () => void, onOpenProfile: () => void }) => {
  const [nearestStop, setNearestStop] = useState("University Town");
  const [selectedRoute, setSelectedRoute] = useState<any>(null);
  const [showToast, setShowToast] = useState(false);
  const [selectedActivity, setSelectedActivity] = useState<any>(null);
  
  // State for Personalized Recommendation
  const [planInput, setPlanInput] = useState("");
  const [recommendation, setRecommendation] = useState<any>(null);

  const handleGetRecommendation = () => {
    if (!planInput.trim()) return;
    const dest = planInput.toLowerCase();
    
    // Simple logic to mock recommendations based on input
    let rec = {
      text: `Take Bus D1 (3 min wait). It's the fastest way to ${planInput}!`,
      tag: 'Fastest ⚡'
    };

    if (dest.includes('library') || dest.includes('study')) {
      rec = {
        text: `It's a nice day! Walking to ${planInput} takes 15 mins and earns 50 Green Pts.`,
        tag: 'Eco-Choice 🌱'
      };
    } else if (dest.includes('gym') || dest.includes('sport')) {
       rec = {
        text: `Warm up with a light jog to ${planInput}. It's only 1.2km away!`,
        tag: 'Healthy 🏃'
      };
    }

    setRecommendation(rec);
    setPlanInput("");
  };

  const handleAddToRoutes = () => {
    setShowToast(true);
    setTimeout(() => {
      setShowToast(false);
      setSelectedRoute(null);
    }, 2000);
  };

  if (selectedActivity) {
      return <ActivityDetailModal activity={selectedActivity} onClose={() => setSelectedActivity(null)} />;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', position: 'relative' }}>
      
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '24px', color: theme.primary, fontWeight: 800 }}>Hello, Alex</h1>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: theme.subtext, fontSize: '13px', marginTop: '4px' }}>
            <Icons.MapPin />
            <span>Near {nearestStop}</span>
          </div>
        </div>
        <div onClick={onOpenProfile} style={{ cursor: 'pointer', width: '40px', height: '40px', borderRadius: '50%', background: '#e2e8f0', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <span style={{ fontSize: '18px' }}>🎓</span>
        </div>
      </div>

      {/* Personalized Recommendation Widget */}
      <div style={{ 
        background: 'white', borderRadius: '20px', padding: '16px',
        boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', gap: '12px'
      }}>
         {!recommendation ? (
           <>
             <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
               <Icons.Sparkles />
               <span style={{ fontWeight: 600, fontSize: '14px' }}>Where to today?</span>
             </div>
             <div style={{ display: 'flex', gap: '8px' }}>
                <input 
                  type="text" 
                  value={planInput}
                  onChange={(e) => setPlanInput(e.target.value)}
                  placeholder="e.g. Science Library, Gym..." 
                  style={{ 
                    flex: 1, padding: '12px', borderRadius: '12px', 
                    border: `1px solid ${theme.border}`, fontSize: '13px', outline: 'none',
                    background: '#f8fafc'
                  }}
                  onKeyDown={(e) => e.key === 'Enter' && handleGetRecommendation()}
                />
                <button 
                  onClick={handleGetRecommendation}
                  style={{ 
                    padding: '0 16px', borderRadius: '12px', background: theme.primary, 
                    color: 'white', border: 'none', fontWeight: 600, cursor: 'pointer' 
                  }}
                >
                  Go
                </button>
             </div>
           </>
         ) : (
           <div style={{ animation: 'slideUp 0.3s ease-out' }}>
             <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span style={{ fontSize: '12px', color: theme.subtext, fontWeight: 600 }}>RECOMMENDATION</span>
                <span style={{ fontSize: '10px', background: '#dcfce7', color: '#166534', padding: '2px 8px', borderRadius: '10px', fontWeight: 700 }}>
                  {recommendation.tag}
                </span>
             </div>
             <p style={{ margin: 0, fontSize: '14px', lineHeight: '1.5', color: theme.text }}>
               {recommendation.text}
             </p>
             <div 
               onClick={() => setRecommendation(null)}
               style={{ marginTop: '12px', fontSize: '12px', color: theme.primary, fontWeight: 600, cursor: 'pointer', textAlign: 'center' }}
             >
               Ask another destination
             </div>
           </div>
         )}
      </div>

      {/* New Stats Row: Community & Personal */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
         <div style={{ background: theme.surface, padding: '12px', borderRadius: '16px', border: `1px solid ${theme.border}`, display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <div style={{ fontSize: '11px', color: theme.subtext, display: 'flex', alignItems: 'center', gap: '4px' }}>
               <Icons.Award /> Monthly
            </div>
            <div style={{ fontSize: '20px', fontWeight: '800', color: theme.text }}>850 <span style={{fontSize: '12px', fontWeight: 500}}>pts</span></div>
            <div style={{ fontSize: '10px', color: theme.primary }}>+150 this week</div>
         </div>
         <div style={{ background: theme.surface, padding: '12px', borderRadius: '16px', border: `1px solid ${theme.border}`, display: 'flex', flexDirection: 'column', gap: '4px' }}>
             <div style={{ fontSize: '11px', color: theme.subtext, display: 'flex', alignItems: 'center', gap: '4px' }}>
               <Icons.Users /> SoC Score
            </div>
            <div style={{ fontSize: '20px', fontWeight: '800', color: '#3b82f6' }}>4,520</div>
            <div style={{ fontSize: '10px', color: theme.subtext }}>Rank #1 🏆</div>
         </div>
      </div>

      {/* Hero Card - Next Bus */}
      <div style={{ 
        background: `linear-gradient(135deg, ${theme.primary} 0%, #047857 100%)`, 
        borderRadius: '24px', 
        padding: '24px', 
        color: 'white',
        boxShadow: '0 10px 20px -5px rgba(5, 150, 105, 0.4)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
          <div>
            <div style={{ fontSize: '12px', opacity: 0.9, marginBottom: '4px' }}>NEXT ARRIVAL</div>
            <div style={{ fontSize: '32px', fontWeight: 'bold' }}>D1</div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>to COM2 / Biz</div>
          </div>
          <div style={{ background: 'rgba(255,255,255,0.2)', padding: '8px 12px', borderRadius: '12px', backdropFilter: 'blur(4px)' }}>
            <div style={{ fontSize: '20px', fontWeight: 'bold' }}>2 <span style={{ fontSize: '12px' }}>min</span></div>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', opacity: 0.8 }}>
          <Icons.Leaf />
          <span>Saving 120g CO2 vs Taxi</span>
        </div>
      </div>

      {/* Interactive Map Widget */}
      <div style={{ 
        height: '200px', 
        borderRadius: '20px', 
        position: 'relative', 
        overflow: 'hidden',
        border: `2px solid white`,
        boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)'
      }}>
        {/* Render Simplified Cartoon Map */}
        <div style={{ position: 'absolute', inset: 0, transform: 'scale(1.2)' }}>
          <CartoonMap simplified={true} />
        </div>
        
        <button 
          onClick={onOpenMap}
          style={{ 
            position: 'absolute', bottom: '12px', right: '12px', 
            background: 'white', border: 'none', borderRadius: '8px', 
            padding: '6px 12px', fontSize: '12px', fontWeight: 600,
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)', color: theme.text,
            display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer'
          }}>
          <Icons.MapPin /> Open Map
        </button>
      </div>

      {/* Monthly Highlights (Activities) */}
      <div>
         <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
           <h3 style={{ margin: 0, fontSize: '16px', color: theme.text }}>Monthly Highlights</h3>
           <span onClick={onOpenActivities} style={{ fontSize: '12px', color: theme.primary, fontWeight: 700, cursor: 'pointer' }}>See All</span>
         </div>
         <div style={{ display: 'flex', gap: '12px', overflowX: 'auto', paddingBottom: '4px' }}>
            {ACTIVITIES.slice(0, 2).map(act => (
              <div 
                key={act.id} 
                onClick={() => setSelectedActivity(act)}
                style={{ 
                  minWidth: '200px', background: 'white', padding: '12px', borderRadius: '12px',
                  border: `1px solid ${theme.border}`, display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer'
                }}
              >
                 <div style={{ fontSize: '24px' }}>{act.icon}</div>
                 <div>
                    <div style={{ fontWeight: 600, fontSize: '14px' }}>{act.title}</div>
                    <div style={{ fontSize: '11px', color: theme.subtext }}>{act.date} • {act.desc}</div>
                 </div>
              </div>
            ))}
         </div>
      </div>

      {/* Scenic Campus Walks */}
      <div>
         <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
           <h3 style={{ margin: 0, fontSize: '16px', color: theme.text }}>Scenic Campus Walks</h3>
         </div>
         <div style={{ display: 'flex', gap: '16px', overflowX: 'auto', paddingBottom: '10px' }}>
           {WALKING_ROUTES.map(route => (
             <div 
               key={route.id} 
               onClick={() => setSelectedRoute(route)}
               style={{ 
                  minWidth: '140px', height: '160px', borderRadius: '16px', 
                  background: route.bg,
                  padding: '16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
                  cursor: 'pointer', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', flexShrink: 0
               }}
             >
                <div style={{ background: 'rgba(255,255,255,0.3)', width: 'fit-content', padding: '4px 8px', borderRadius: '8px', fontSize: '10px', fontWeight: 600 }}>
                  {route.time}
                </div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: '14px', marginBottom: '4px', lineHeight: '1.2' }}>{route.title}</div>
                  <div style={{ fontSize: '10px', opacity: 0.8 }}>{route.dist}</div>
                </div>
             </div>
           ))}
         </div>
      </div>

      {/* Route Detail Modal / Overlay */}
      {selectedRoute && (
        <div style={{ 
          position: 'absolute', inset: -20, zIndex: 100, 
          background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'flex-end'
        }}>
          <div style={{ 
            width: '100%', background: 'white', borderRadius: '24px 24px 0 0', 
            padding: '24px', animation: 'slideUp 0.3s ease-out'
          }}>
             <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                <div>
                  <div style={{ fontSize: '12px', color: theme.primary, fontWeight: 700, textTransform: 'uppercase', marginBottom: '4px' }}>Featured Route</div>
                  <h2 style={{ margin: 0, fontSize: '24px' }}>{selectedRoute.title}</h2>
                </div>
                <div onClick={() => setSelectedRoute(null)} style={{ padding: '4px', cursor: 'pointer', color: theme.subtext }}>
                  <Icons.X />
                </div>
             </div>

             <div style={{ display: 'flex', gap: '8px', marginBottom: '20px' }}>
                {selectedRoute.tags.map((tag: string) => (
                  <span key={tag} style={{ fontSize: '11px', background: '#f1f5f9', padding: '4px 8px', borderRadius: '8px', color: theme.subtext }}>{tag}</span>
                ))}
             </div>

             <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px', background: '#f8fafc', padding: '16px', borderRadius: '16px' }}>
                 <div style={{ textAlign: 'center' }}>
                    <div style={{ fontWeight: 700, fontSize: '16px' }}>{selectedRoute.time}</div>
                    <div style={{ fontSize: '10px', color: theme.subtext }}>Duration</div>
                 </div>
                 <div style={{ textAlign: 'center', borderLeft: '1px solid #e2e8f0', borderRight: '1px solid #e2e8f0', padding: '0 20px' }}>
                    <div style={{ fontWeight: 700, fontSize: '16px' }}>{selectedRoute.dist}</div>
                    <div style={{ fontSize: '10px', color: theme.subtext }}>Distance</div>
                 </div>
                 <div style={{ textAlign: 'center' }}>
                    <div style={{ fontWeight: 700, fontSize: '16px' }}>{selectedRoute.calories}</div>
                    <div style={{ fontSize: '10px', color: theme.subtext }}>Burn</div>
                 </div>
             </div>

             <p style={{ fontSize: '14px', color: theme.subtext, lineHeight: '1.6', marginBottom: '24px' }}>
               {selectedRoute.desc}
             </p>

             <StyledButton onClick={handleAddToRoutes}>
                <Icons.Plus /> Add to My Routes
             </StyledButton>
          </div>
        </div>
      )}

      {/* Toast Notification */}
      {showToast && <CustomToast message="Route Added!" />}

    </div>
  );
};

const MascotLion = ({ outfit }: { outfit: { head: string, face: string, body: string } }) => {
  const [blink, setBlink] = useState(false);
  const [happy, setHappy] = useState(false);

  useEffect(() => {
    // Random blinking logic
    const blinkInterval = setInterval(() => {
       setBlink(true);
       setTimeout(() => setBlink(false), 200);
    }, 4000);
    return () => clearInterval(blinkInterval);
  }, []);

  const handleClick = () => {
    setHappy(true);
    setTimeout(() => setHappy(false), 1000);
  };

  return (
    <div 
      onClick={handleClick}
      style={{ 
        position: 'relative', width: '160px', height: '160px', margin: '0 auto', 
        cursor: 'pointer', transition: 'transform 0.2s',
        animation: happy ? 'jump 0.5s ease-in-out' : 'breathe 3s ease-in-out infinite'
      }}>
      {/* Shadow */}
      <div style={{ position: 'absolute', bottom: '10px', left: '30px', width: '100px', height: '10px', background: 'rgba(0,0,0,0.1)', borderRadius: '50%' }}></div>
      
      {/* SVG Lion Body */}
      <svg viewBox="0 0 200 200" width="100%" height="100%">
        {/* Tail - Waving Animation if happy */}
        <g style={{ transformOrigin: '160px 140px', animation: happy ? 'wave 1s ease-in-out infinite' : 'none' }}>
           <path d="M160 140 Q 180 120 170 100 Q 160 80 170 80" stroke="#F59E0B" strokeWidth="8" fill="none" strokeLinecap="round" />
           <circle cx="170" cy="80" r="5" fill="#B45309" />
        </g>

        {/* Base Body */}
        <rect x="60" y="100" width="80" height="70" rx="20" fill="#F59E0B" />
        <path d="M60 100 Q 100 120 140 100" fill="#FCD34D" opacity="0.6" />
        
        {/* Legs */}
        <path d="M70 160 L 70 180 A 5 5 0 0 0 80 180 L 80 160" fill="#F59E0B" />
        <path d="M120 160 L 120 180 A 5 5 0 0 0 130 180 L 130 160" fill="#F59E0B" />

        {/* Head Group */}
        <g>
          {/* Head Base */}
          <circle cx="100" cy="80" r="45" fill="#F59E0B" />
          <circle cx="100" cy="80" r="35" fill="#FCD34D" /> {/* Inner face */}
          
          {/* Ears */}
          <circle cx="65" cy="55" r="12" fill="#F59E0B" />
          <circle cx="65" cy="55" r="8" fill="#FCD34D" />
          <circle cx="135" cy="55" r="12" fill="#F59E0B" />
          <circle cx="135" cy="55" r="8" fill="#FCD34D" />
          
          {/* Face Features */}
          {/* Eyes - Blinking Logic */}
          <g style={{ transformOrigin: '100px 75px', animation: blink ? 'blink 0.2s linear' : 'none' }}>
             <circle cx="85" cy="75" r="5" fill="#374151" /> {/* Eye L */}
             <circle cx="115" cy="75" r="5" fill="#374151" /> {/* Eye R */}
          </g>

          <path d={happy ? "M90 90 Q 100 100 110 90" : "M95 90 Q 100 95 105 90"} stroke="#374151" strokeWidth="3" fill="none" /> {/* Mouth */}
          <circle cx="100" cy="85" r="4" fill="#B45309" /> {/* Nose */}

          {/* --- Outfits (Rendered via SVG conditionally) --- */}
          
          {/* Body Items */}
          {outfit.body === 'shirt_nus' && (
             <rect x="62" y="105" width="76" height="50" rx="10" fill="white" /> 
          )}
          {outfit.body === 'shirt_nus' && (
             <text x="100" y="135" textAnchor="middle" fontSize="16" fontWeight="bold" fill="#F97316">NUS</text>
          )}
          
          {outfit.body === 'shirt_hoodie' && (
             <rect x="58" y="102" width="84" height="60" rx="15" fill="#3B82F6" />
          )}
          {outfit.body === 'shirt_hoodie' && (
             <path d="M80 102 L 80 140" stroke="rgba(0,0,0,0.1)" strokeWidth="2" />
          )}

          {/* Head Items */}
          {outfit.head === 'hat_grad' && (
            <g>
              <rect x="60" y="35" width="80" height="10" fill="#1e293b" />
              <polygon points="70,35 130,35 100,10" fill="#1e293b" />
              <line x1="130" y1="35" x2="135" y2="60" stroke="#FCD34D" strokeWidth="2" />
            </g>
          )}
          {outfit.head === 'hat_cap' && (
            <g>
              <path d="M60 50 Q 100 20 140 50" fill="#F97316" />
              <rect x="130" y="45" width="20" height="5" fill="#F97316" rx="2" />
            </g>
          )}

          {/* Face Items */}
          {outfit.face === 'glasses_sun' && (
            <g>
               <rect x="75" y="70" width="20" height="10" rx="2" fill="black" />
               <rect x="105" y="70" width="20" height="10" rx="2" fill="black" />
               <line x1="95" y1="75" x2="105" y2="75" stroke="black" strokeWidth="2" />
            </g>
          )}
        </g>

      </svg>
    </div>
  );
};

const ProfileScreen = ({ onNavigate, points, setPoints, outfit, setOutfit, inventory, setInventory }: any) => {
  const [activeTab, setActiveTab] = useState('closet');
  const [successModal, setSuccessModal] = useState<{open: boolean, msg: string}>({ open: false, msg: '' });

  const handleItemClick = (item: any) => {
    const isOwned = inventory.includes(item.id);
    const isEquipped = outfit[item.type] === item.id;

    if (isEquipped) {
       // Toggle off if currently wearing
       setOutfit({ ...outfit, [item.type]: 'none' });
       return;
    }

    if (isOwned) {
      // Just equip
      setOutfit({ ...outfit, [item.type]: item.id });
    } else {
      // Try to buy
      if (points >= item.cost) {
        setPoints(points - item.cost);
        setInventory([...inventory, item.id]);
        setOutfit({ ...outfit, [item.type]: item.id });
        setSuccessModal({ open: true, msg: `Bought & Equipped ${item.name}!` });
      } else {
         // Show error or alert
         alert("Not enough points!");
      }
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', position: 'relative', gap: '20px', minHeight: '100%' }}>
      
      {/* Header & Mascot Stage */}
      <div style={{ 
        background: 'white', 
        borderRadius: '24px', 
        padding: '24px 20px 20px', 
        boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
        textAlign: 'center',
        position: 'relative',
        overflow: 'hidden',
        flexShrink: 0
      }}>
        {/* Background Blob */}
        <div style={{ position: 'absolute', top: '-50px', left: '-50px', width: '200px', height: '200px', background: theme.bg, borderRadius: '50%', zIndex: 0 }}></div>
        
        <div style={{ position: 'relative', zIndex: 1 }}>
           <h2 style={{ margin: '0 0 4px 0', fontSize: '18px', color: theme.primary, fontWeight: 700 }}>Mascot Studio</h2>
           <div style={{ fontSize: '13px', color: theme.subtext, marginBottom: '20px' }}>Customize your LiNUS (Tap him!)</div>
           
           <MascotLion outfit={outfit} />
           
           <div style={{ marginTop: '20px' }}>
             <h3 style={{ margin: '0', fontSize: '20px', fontWeight: 800 }}>Alex Tan</h3>
             <p style={{ margin: '0', fontSize: '12px', color: theme.subtext }}>Computer Science • Year 2</p>
           </div>
        </div>
        
        <div onClick={() => onNavigate('settings')} style={{ position: 'absolute', top: '16px', right: '16px', padding: '8px', cursor: 'pointer', color: theme.subtext, zIndex: 10 }}>
           <Icons.Settings />
        </div>
      </div>

      <div style={{ 
        background: `linear-gradient(135deg, #1e293b 0%, #0f172a 100%)`, 
        borderRadius: '20px', padding: '20px', color: 'white',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        boxShadow: '0 8px 16px -4px rgba(15, 23, 42, 0.3)',
        flexShrink: 0
      }}>
        <div>
           <div style={{ fontSize: '12px', opacity: 0.7, marginBottom: '4px' }}>GREEN POINTS</div>
           <div style={{ fontSize: '28px', fontWeight: 'bold' }}>{points}</div>
        </div>
        <button 
          onClick={() => onNavigate('vouchers')}
          style={{ 
            background: 'rgba(255,255,255,0.2)', color: 'white', border: 'none', 
            padding: '10px 16px', borderRadius: '12px', fontSize: '13px', fontWeight: 600,
            cursor: 'pointer', backdropFilter: 'blur(4px)'
          }}
        >
          Redeem <Icons.ChevronRight />
        </button>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', gap: '16px', marginBottom: '16px', borderBottom: `1px solid ${theme.border}`, overflowX: 'auto', flexShrink: 0 }}>
          {['closet', 'badges', 'history'].map(t => (
            <div 
              key={t} 
              onClick={() => setActiveTab(t)}
              style={{ 
                paddingBottom: '12px', cursor: 'pointer', textTransform: 'capitalize',
                fontWeight: activeTab === t ? 700 : 500,
                color: activeTab === t ? theme.primary : theme.subtext,
                borderBottom: activeTab === t ? `2px solid ${theme.primary}` : 'none',
                whiteSpace: 'nowrap'
              }}
            >
              {t}
            </div>
          ))}
        </div>

        {activeTab === 'closet' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', paddingBottom: '20px' }}>
            {SHOP_ITEMS.map(item => {
                const isOwned = inventory.includes(item.id);
                const isEquipped = outfit[item.type] === item.id;
                const canAfford = points >= item.cost;
                
                return (
                  <div key={item.id} 
                    onClick={() => handleItemClick(item)}
                    style={{ 
                      background: 'white', padding: '16px', borderRadius: '16px',
                      border: isEquipped ? `2px solid ${theme.primary}` : '2px solid transparent',
                      opacity: !isOwned && !canAfford ? 0.6 : 1,
                      cursor: 'pointer', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px',
                      position: 'relative', boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                    }}
                  >
                    {isEquipped && <div style={{ position: 'absolute', top: 8, right: 8, color: theme.primary }}><Icons.Check /></div>}
                    
                    <div style={{ fontSize: '32px' }}>{item.icon}</div>
                    <div style={{ fontSize: '13px', fontWeight: 600 }}>{item.name}</div>
                    
                    {isEquipped ? (
                      <div style={{ fontSize: '11px', color: theme.primary, fontWeight: 700, marginTop: '4px' }}>Equipped</div>
                    ) : isOwned ? (
                      <div style={{ fontSize: '11px', color: theme.subtext, fontWeight: 700, marginTop: '4px' }}>Owned</div>
                    ) : (
                      <div style={{ fontSize: '11px', background: '#f1f5f9', padding: '4px 10px', borderRadius: '8px', fontWeight: 600, marginTop: '4px' }}>{item.cost} pts</div>
                    )}
                  </div>
                );
            })}
          </div>
        )}
        
        {activeTab === 'badges' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', paddingBottom: '20px' }}>
            {ACHIEVEMENTS.map(badge => (
              <div key={badge.id} style={{ 
                background: 'white', padding: '16px 8px', borderRadius: '16px',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px',
                opacity: badge.unlocked ? 1 : 0.5,
                filter: badge.unlocked ? 'none' : 'grayscale(1)',
                textAlign: 'center', boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
              }}>
                  <div style={{ fontSize: '32px' }}>{badge.icon}</div>
                  <div style={{ fontWeight: 600, fontSize: '12px' }}>{badge.name}</div>
                  <div style={{ fontSize: '10px', color: theme.subtext }}>{badge.desc}</div>
                  {!badge.unlocked && <div style={{ marginTop: '4px', color: theme.subtext }}><Icons.Lock /></div>}
              </div>
            ))}
          </div>
        )}

        {activeTab === 'history' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', paddingBottom: '20px' }}>
            {HISTORY.map(item => (
              <div key={item.id} style={{ 
                background: 'white', padding: '16px', borderRadius: '16px',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
              }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '14px' }}>{item.action}</div>
                    <div style={{ fontSize: '11px', color: theme.subtext }}>{item.time}</div>
                  </div>
                  <div style={{ 
                    fontWeight: 700, fontSize: '14px',
                    color: item.type === 'earn' ? theme.primary : '#ef4444'
                  }}>
                    {item.points}
                  </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <SuccessModal 
        isOpen={successModal.open} 
        onClose={() => setSuccessModal({ ...successModal, open: false })} 
        message={successModal.msg}
      />
    </div>
  );
};

const RoutesScreen = () => {
  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column' }}>
      <div style={{ padding: '24px 20px', background: 'white', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}` }}>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>Live Bus Routes</h2>
      </div>
      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px', overflowY: 'auto' }}>
        {ROUTES.map(route => (
           <div key={route.id} style={{ background: 'white', borderRadius: '16px', padding: '16px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', borderLeft: `4px solid ${route.color}` }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                 <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div style={{ background: route.color, color: 'white', padding: '4px 10px', borderRadius: '8px', fontWeight: 700, fontSize: '14px' }}>{route.name}</div>
                    <StatusBadge status={route.status} />
                 </div>
                 <div style={{ textAlign: 'right' }}>
                    <div style={{ fontWeight: 700, fontSize: '16px' }}>{route.time}</div>
                    <div style={{ fontSize: '10px', color: theme.subtext }}>Wait Time</div>
                 </div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: theme.text, marginBottom: '8px' }}>
                 <div style={{ width: '8px', height: '8px', borderRadius: '50%', border: `2px solid ${route.color}` }} />
                 {route.from}
                 <span style={{ color: theme.subtext }}>➔</span>
                 {route.to}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '11px', color: theme.subtext, marginTop: '8px', paddingTop: '8px', borderTop: `1px solid #f1f5f9` }}>
                 <Icons.Users /> Crowd: <span style={{ fontWeight: 600, color: theme.text }}>{route.crowd}</span>
              </div>
           </div>
        ))}
      </div>
    </div>
  );
};

const CommunityScreen = () => {
  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column' }}>
       <div style={{ padding: '24px 20px', background: 'white', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}` }}>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>Faculty Leaderboard</h2>
      </div>
      <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px', overflowY: 'auto' }}>
         <div style={{ background: '#ecfdf5', borderRadius: '16px', padding: '16px', display: 'flex', alignItems: 'center', gap: '16px', border: `1px solid ${theme.primary}` }}>
             <div style={{ fontSize: '32px' }}>🏆</div>
             <div>
                <div style={{ fontSize: '12px', color: '#166534', fontWeight: 700, textTransform: 'uppercase' }}>Current Leader</div>
                <div style={{ fontSize: '16px', fontWeight: 700, color: '#14532d' }}>School of Computing</div>
                <div style={{ fontSize: '11px', color: '#15803d' }}>4,520 pts • +12% this week</div>
             </div>
         </div>

         <h3 style={{ fontSize: '14px', margin: '8px 0 0', color: theme.subtext }}>Rankings</h3>
         
         {COMMUNITIES.map((com, index) => (
            <div key={com.name} style={{ background: 'white', borderRadius: '16px', padding: '16px', display: 'flex', alignItems: 'center', gap: '16px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
               <div style={{ width: '30px', height: '30px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, color: index < 3 ? theme.primary : theme.subtext, fontSize: '16px' }}>
                 #{com.rank}
               </div>
               <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>{com.name}</div>
                  <div style={{ width: '100%', height: '6px', background: '#f1f5f9', borderRadius: '3px', marginTop: '6px', overflow: 'hidden' }}>
                     <div style={{ width: `${(com.score / 5000) * 100}%`, height: '100%', background: com.color }} />
                  </div>
               </div>
               <div style={{ textAlign: 'right' }}>
                  <div style={{ fontWeight: 700, fontSize: '14px' }}>{com.score}</div>
                  <div style={{ fontSize: '10px', color: '#16a34a' }}>{com.change}</div>
               </div>
            </div>
         ))}
      </div>
    </div>
  );
};

const ChatScreen = () => {
  const [messages, setMessages] = useState<{role: string, text: string}[]>([
    { role: 'model', text: 'Hello! I\'m LiNUS. How can I help you navigate campus today? 🦁' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);
  const chatSessionRef = useRef<Chat | null>(null);

  useEffect(() => {
    // Initialize chat session
    const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
    chatSessionRef.current = ai.chats.create({
      model: 'gemini-3-flash-preview',
      config: {
        systemInstruction: "You are LiNUS, the friendly lion mascot of NUS (National University of Singapore). You help students with campus navigation, finding bus routes (D1, D2, A1, A2, BTC, K, etc.), sustainability tips (Green Points), and finding food. Be cheerful, concise, and use emojis. Keep responses under 50 words unless asked for more details.",
      }
    });
  }, []);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || !chatSessionRef.current) return;
    
    const userMsg = input;
    setInput('');
    setMessages(prev => [...prev, { role: 'user', text: userMsg }]);
    setLoading(true);

    try {
      const response = await chatSessionRef.current.sendMessage({ message: userMsg });
      const text = response.text;
      setMessages(prev => [...prev, { role: 'model', text: text || "I didn't get that, sorry!" }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { role: 'model', text: 'Oops! I had trouble connecting. Please try again.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column' }}>
       <div style={{ padding: '24px 20px', background: 'white', display: 'flex', alignItems: 'center', gap: '12px', borderBottom: `1px solid ${theme.border}` }}>
        <h2 style={{ fontSize: '18px', margin: 0, color: theme.primary, fontWeight: 700 }}>Chat with LiNUS</h2>
      </div>
      
      <div ref={scrollRef} style={{ flex: 1, overflowY: 'auto', padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
         {messages.map((msg, idx) => (
           <div key={idx} style={{ 
             alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
             maxWidth: '80%',
             background: msg.role === 'user' ? theme.primary : 'white',
             color: msg.role === 'user' ? 'white' : theme.text,
             padding: '12px 16px',
             borderRadius: msg.role === 'user' ? '20px 20px 4px 20px' : '20px 20px 20px 4px',
             boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
             fontSize: '14px',
             lineHeight: '1.5'
           }}>
             {msg.text}
           </div>
         ))}
         {loading && (
           <div style={{ alignSelf: 'flex-start', background: 'white', padding: '12px 16px', borderRadius: '20px 20px 20px 4px', fontSize: '12px', color: theme.subtext, display: 'flex', alignItems: 'center', gap: '8px' }}>
             <Spinner /> Thinking...
           </div>
         )}
      </div>

      <div style={{ padding: '16px', background: 'white', borderTop: `1px solid ${theme.border}`, display: 'flex', gap: '8px' }}>
         <input 
           type="text" 
           value={input}
           onChange={(e) => setInput(e.target.value)}
           placeholder="Ask about buses, food..."
           onKeyDown={(e) => e.key === 'Enter' && handleSend()}
           style={{ flex: 1, padding: '12px', borderRadius: '24px', border: `1px solid ${theme.border}`, outline: 'none', fontSize: '14px', background: '#f8fafc' }}
         />
         <button 
           onClick={handleSend}
           disabled={loading || !input.trim()}
           style={{ 
             width: '44px', height: '44px', borderRadius: '50%', background: theme.primary, 
             color: 'white', border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center',
             cursor: 'pointer', opacity: loading || !input.trim() ? 0.5 : 1
           }}
         >
           <Icons.Send />
         </button>
      </div>
    </div>
  );
};

// --- Mobile Wrapper & App ---

const MobileApp = () => {
  const [activeTab, setActiveTab] = useState('home');
  const [appState, setAppState] = useState<'login' | 'onboarding' | 'app'>('login');
  
  // Lifted State
  const [points, setPoints] = useState(1250);
  const [outfit, setOutfit] = useState({ head: 'none', face: 'none', body: 'none' });
  const [inventory, setInventory] = useState<string[]>([]); // New inventory state

  const handleLogin = () => setAppState('app');
  const handleSignUp = () => setAppState('onboarding');
  const handleOnboardingComplete = () => setAppState('app');
  const handleLogout = () => {
    setAppState('login');
    setActiveTab('home');
  };

  const renderScreen = () => {
    if (appState === 'login') return <LoginScreen onLogin={handleLogin} onSignUp={handleSignUp} />;
    if (appState === 'onboarding') return <Onboarding onComplete={handleOnboardingComplete} />;

    switch(activeTab) {
      case 'home': return <HomeScreen onOpenMap={() => setActiveTab('map')} onOpenActivities={() => setActiveTab('activities')} onOpenProfile={() => setActiveTab('profile')} />;
      case 'map': return <MapScreen onBack={() => setActiveTab('home')} />;
      case 'routes': return <RoutesScreen />;
      case 'community': return <CommunityScreen />;
      case 'chat': return <ChatScreen />;
      case 'profile': return <ProfileScreen onNavigate={(screen: string) => setActiveTab(screen)} points={points} setPoints={setPoints} outfit={outfit} setOutfit={setOutfit} inventory={inventory} setInventory={setInventory} />;
      case 'vouchers': return <VoucherScreen onBack={() => setActiveTab('profile')} points={points} setPoints={setPoints} />;
      case 'activities': return <ActivityScreen onBack={() => setActiveTab('home')} />; // New Activity Screen
      case 'settings': return <SettingsScreen onBack={() => setActiveTab('profile')} onLogout={handleLogout} />;
      case 'about': return <AboutScreen onBack={() => setActiveTab('profile')} />;
      default: return <HomeScreen onOpenMap={() => setActiveTab('map')} onOpenActivities={() => setActiveTab('activities')} onOpenProfile={() => setActiveTab('profile')} />;
    }
  };

  // Only show nav bar in main app views
  const showNavBar = appState === 'app' && ['home', 'routes', 'community', 'chat', 'profile'].includes(activeTab);

  return (
    <div style={mobileStyles.phoneContainer}>
      <style>{animationStyles}</style>
      {/* Phone Hardware UI */}
      <div style={mobileStyles.notch} />
      <div style={mobileStyles.statusBar}>
        <span>9:41</span>
        <div style={{ display: 'flex', gap: '4px' }}>
          <Icons.Wifi />
          <span>100%</span>
        </div>
      </div>

      {/* Screen Content */}
      <div style={{ ...mobileStyles.content, paddingBottom: showNavBar ? '100px' : '20px' }}>
        {renderScreen()}
      </div>

      {/* Bottom Navigation */}
      {showNavBar && (
        <div style={mobileStyles.bottomNav}>
          <div 
            style={{ ...mobileStyles.navItem, ...(activeTab === 'home' ? mobileStyles.navItemActive : {}) }}
            onClick={() => setActiveTab('home')}
          >
            <Icons.Navigation />
            <span>Home</span>
          </div>
          <div 
            style={{ ...mobileStyles.navItem, ...(activeTab === 'routes' ? mobileStyles.navItemActive : {}) }}
            onClick={() => setActiveTab('routes')}
          >
            <Icons.Bus />
            <span>Routes</span>
          </div>
           <div 
            style={{ ...mobileStyles.navItem, ...(activeTab === 'community' ? mobileStyles.navItemActive : {}) }}
            onClick={() => setActiveTab('community')}
          >
            <Icons.Users />
            <span>Community</span>
          </div>
          <div 
            style={{ ...mobileStyles.navItem, ...(activeTab === 'chat' ? mobileStyles.navItemActive : {}) }}
            onClick={() => setActiveTab('chat')}
          >
            <Icons.MessageCircle />
            <span>LiNUS</span>
          </div>
          <div 
            style={{ ...mobileStyles.navItem, ...(activeTab === 'profile' ? mobileStyles.navItemActive : {}) }}
            onClick={() => setActiveTab('profile')}
          >
            <Icons.User />
            <span>Profile</span>
          </div>
        </div>
      )}
    </div>
  );
};

const rootElement = document.getElementById('root');
if (rootElement) {
  const root = createRoot(rootElement);
  root.render(<MobileApp />);
}