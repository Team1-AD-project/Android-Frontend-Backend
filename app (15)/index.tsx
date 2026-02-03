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
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
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
  ),
  Share: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" x2="15.42" y1="13.51" y2="17.49"/><line x1="15.41" x2="8.59" y1="6.51" y2="10.49"/></svg>
  ),
  Download: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" x2="12" y1="15" y2="3"/></svg>
  ),
  Search: () => (
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
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

// Added Faculty Data for Signup Flow
const FACULTIES = [
  { id: 'eng', name: 'Engineering', color: '#3b82f6', slogan: 'Building the Future 🛠️', outfit: { head: 'hat_helmet', face: 'none', body: 'body_plaid' } },
  { id: 'biz', name: 'Business School', color: '#eab308', slogan: 'Leading the Way 💼', outfit: { head: 'none', face: 'none', body: 'body_suit' } },
  { id: 'arts', name: 'Arts & Social Sci', color: '#f97316', slogan: 'Create & Inspire 🎨', outfit: { head: 'hat_beret', face: 'none', body: 'body_coat' } }, // Reusing coat for smock look
  { id: 'med', name: 'Medicine', color: '#10b981', slogan: 'Saving Lives 🩺', outfit: { head: 'none', face: 'none', body: 'body_coat' } },
  { id: 'sci', name: 'Science', color: '#6366f1', slogan: 'Discovering Truth 🧪', outfit: { head: 'none', face: 'face_goggles', body: 'body_coat' } },
];

const SHOP_ITEMS = [
  { id: 'hat_grad', name: 'Grad Cap', type: 'head', cost: 500, icon: '🎓' },
  { id: 'hat_cap', name: 'Orange Cap', type: 'head', cost: 200, icon: '🧢' },
  { id: 'hat_helmet', name: 'Safety Helmet', type: 'head', cost: 300, icon: '⛑️' },
  { id: 'hat_beret', name: 'Artist Beret', type: 'head', cost: 300, icon: '🎨' },
  { id: 'glasses_sun', name: 'Shades', type: 'face', cost: 300, icon: '🕶️' },
  { id: 'face_goggles', name: 'Safety Goggles', type: 'face', cost: 250, icon: '🥽' },
  { id: 'shirt_nus', name: 'NUS Tee', type: 'body', cost: 400, icon: '👕' },
  { id: 'shirt_hoodie', name: 'Blue Hoodie', type: 'body', cost: 600, icon: '🧥' },
  { id: 'body_plaid', name: 'Engin Plaid', type: 'body', cost: 400, icon: '👔' },
  { id: 'body_suit', name: 'Biz Suit', type: 'body', cost: 500, icon: '🕴️' },
  { id: 'body_coat', name: 'Lab Coat', type: 'body', cost: 450, icon: '🥼' },
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

const INITIAL_ACHIEVEMENTS = [
  { id: '1', name: 'Freshman', icon: '🌱', desc: 'Complete your first eco-trip.', unlocked: true, progress: 1, target: 1, reward: 100 },
  { id: '2', name: 'Bus Pro', icon: '🚌', desc: 'Take 10 Shuttle Bus rides.', unlocked: true, progress: 10, target: 10, reward: 500 },
  { id: '3', name: 'Hiker', icon: '🥾', desc: 'Walk a total of 5km on campus.', unlocked: false, progress: 2.5, target: 5, actionLabel: 'Find Hiking Routes', actionTab: 'routes', reward: 1000 },
  { id: '4', name: 'Carbon Zero', icon: '♻️', desc: 'Save 10kg of CO2 emissions.', unlocked: false, progress: 9.8, target: 10, actionLabel: 'Start Activity', actionTab: 'routes', reward: 800 }, // Close to completion
  { id: '5', name: 'Early Bird', icon: '🌅', desc: 'Complete a trip before 8 AM.', unlocked: true, progress: 1, target: 1, reward: 200 },
  { id: '6', name: 'Night Owl', icon: '🦉', desc: 'Complete a trip after 10 PM.', unlocked: false, progress: 0, target: 1, actionLabel: 'Check Bus Schedule', actionTab: 'home', reward: 300 },
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
  @keyframes slideInRight {
    from { transform: translateX(100%); opacity: 0; }
    to { transform: translateX(0); opacity: 1; }
  }
  @keyframes slideInLeft {
    from { transform: translateX(-100%); opacity: 0; }
    to { transform: translateX(0); opacity: 1; }
  }
  @keyframes fall {
    0% { transform: translateY(-100%) rotate(0deg); opacity: 1; }
    100% { transform: translateY(800px) rotate(720deg); opacity: 0; }
  }
  @keyframes pulse-ring {
    0% { transform: scale(0.33); opacity: 1; }
    80%, 100% { transform: scale(2); opacity: 0; }
  }
  @keyframes dash {
    to { stroke-dashoffset: -100; }
  }
  @keyframes slideUp {
    from { transform: translateY(100%); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
  }
  @keyframes float {
    0% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
    100% { transform: translateY(0px); }
  }
`;


// --- Extracted UI Components ---

// 0. Mascot Component (Consolidated)
const MascotLion = ({ outfit = { head: 'none', face: 'none', body: 'none', badge: 'none' } }: any) => {
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

  const badgeIcon = INITIAL_ACHIEVEMENTS.find(a => a.id === outfit.badge)?.icon || '';

  return (
    <div 
      onClick={handleClick}
      style={{ 
        position: 'relative', width: '100%', height: '100%', margin: '0 auto', 
        cursor: 'pointer', transition: 'transform 0.2s',
        animation: happy ? 'jump 0.5s ease-in-out' : 'breathe 3s ease-in-out infinite'
      }}>
      
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
             <g>
                <rect x="62" y="105" width="76" height="50" rx="10" fill="white" /> 
                <text x="100" y="135" textAnchor="middle" fontSize="16" fontWeight="bold" fill="#F97316">NUS</text>
             </g>
          )}
          
          {outfit.body === 'shirt_hoodie' && (
             <g>
                <rect x="58" y="102" width="84" height="60" rx="15" fill="#3B82F6" />
                <path d="M80 102 L 80 140" stroke="rgba(0,0,0,0.1)" strokeWidth="2" />
             </g>
          )}

          {outfit.body === 'body_suit' && (
             <g>
                <path d="M60 100 L 140 100 L 140 170 L 60 170 Z" fill="#1e293b" />
                <polygon points="100,100 90,130 100,160 110,130" fill="#dc2626" />
                <path d="M60 100 L 90 130 L 60 150" fill="white" opacity="0.1" />
                <path d="M140 100 L 110 130 L 140 150" fill="white" opacity="0.1" />
             </g>
          )}

          {outfit.body === 'body_plaid' && (
             <g>
                <rect x="60" y="100" width="80" height="70" rx="20" fill="#ef4444" />
                <line x1="70" y1="100" x2="70" y2="170" stroke="rgba(0,0,0,0.2)" strokeWidth="4" />
                <line x1="90" y1="100" x2="90" y2="170" stroke="rgba(0,0,0,0.2)" strokeWidth="4" />
                <line x1="110" y1="100" x2="110" y2="170" stroke="rgba(0,0,0,0.2)" strokeWidth="4" />
                <line x1="130" y1="100" x2="130" y2="170" stroke="rgba(0,0,0,0.2)" strokeWidth="4" />
                <line x1="60" y1="120" x2="140" y2="120" stroke="rgba(0,0,0,0.2)" strokeWidth="4" />
                <line x1="60" y1="140" x2="140" y2="140" stroke="rgba(0,0,0,0.2)" strokeWidth="4" />
             </g>
          )}

          {outfit.body === 'body_coat' && (
             <g>
                <rect x="58" y="100" width="84" height="75" rx="15" fill="white" stroke="#e2e8f0" strokeWidth="1" />
                <line x1="100" y1="100" x2="100" y2="175" stroke="#e2e8f0" strokeWidth="2" />
                <path d="M100 100 L 80 120 M100 100 L 120 120" stroke="#cbd5e1" strokeWidth="2" />
             </g>
          )}

          {/* Badge Rendering on Chest */}
          {outfit.badge && outfit.badge !== 'none' && (
             <g transform="translate(115, 140)">
                <circle r="14" fill="white" stroke="#e2e8f0" strokeWidth="1" filter="drop-shadow(0px 2px 2px rgba(0,0,0,0.1))" />
                <text x="0" y="5" textAnchor="middle" fontSize="16">
                  {badgeIcon}
                </text>
             </g>
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
          
          {outfit.head === 'hat_helmet' && (
            <g>
               <path d="M55 55 Q 100 20 145 55" fill="#fbbf24" stroke="#d97706" strokeWidth="2" />
               <rect x="55" y="55" width="90" height="10" rx="2" fill="#fbbf24" stroke="#d97706" strokeWidth="2" />
            </g>
          )}

          {outfit.head === 'hat_beret' && (
            <g>
               <path d="M150 40 Q 120 20 70 45 Q 60 55 130 55 Q 160 55 150 40" fill="#dc2626" />
               <rect x="100" y="20" width="4" height="8" fill="#dc2626" />
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
          
          {outfit.face === 'face_goggles' && (
             <g>
                <rect x="70" y="65" width="25" height="15" rx="5" fill="rgba(147, 197, 253, 0.5)" stroke="#3b82f6" strokeWidth="2" />
                <rect x="105" y="65" width="25" height="15" rx="5" fill="rgba(147, 197, 253, 0.5)" stroke="#3b82f6" strokeWidth="2" />
                <line x1="95" y1="72" x2="105" y2="72" stroke="#3b82f6" strokeWidth="2" />
                <path d="M70 72 L 55 65 M 130 72 L 145 65" stroke="#1e293b" strokeWidth="3" />
             </g>
          )}
        </g>

      </svg>
    </div>
  );
};

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

// 7. Badge Detail Modal (Enriched)
const BadgeDetailModal = ({ badge, onClose, onNavigate }: { badge: any, onClose: () => void, onNavigate: (tab: string) => void }) => {
  const percent = Math.min(100, Math.max(0, (badge.progress / badge.target) * 100));
  
  return (
    <div style={{
      position: 'absolute', inset: 0, zIndex: 200,
      background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center',
      backdropFilter: 'blur(4px)'
    }}>
       <div style={{
        background: 'white', padding: '32px 24px', borderRadius: '32px',
        width: '85%', maxWidth: '320px',
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
        animation: 'popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
        textAlign: 'center', position: 'relative', overflow: 'hidden'
      }}>
          {/* Decorative Background */}
          <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '100px', background: badge.unlocked ? '#dcfce7' : '#f1f5f9', zIndex: 0 }}></div>
          
          <div onClick={onClose} style={{position: 'absolute', top: 16, right: 16, cursor: 'pointer', padding: 8, zIndex: 10, background: 'white', borderRadius: '50%'}}><Icons.X /></div>
          
          <div style={{
            fontSize: '64px', marginBottom: '12px', position: 'relative', zIndex: 1,
            filter: badge.unlocked ? 'drop-shadow(0 10px 10px rgba(0,0,0,0.1))' : 'grayscale(1)', 
            opacity: badge.unlocked ? 1 : 0.6,
            transform: badge.unlocked ? 'scale(1.1)' : 'scale(1)'
          }}>
            {badge.icon}
          </div>
          
          <h2 style={{ fontSize: '22px', fontWeight: 800, margin: '0 0 4px 0', color: theme.text, position: 'relative', zIndex: 1 }}>{badge.name}</h2>
          
          {/* Reward Tag */}
          <div style={{ 
            background: '#fffbeb', color: '#b45309', padding: '4px 12px', borderRadius: '12px', 
            fontSize: '12px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '4px',
            position: 'relative', zIndex: 1
          }}>
             <span>🎁 Reward: {badge.reward} pts</span>
          </div>

          <p style={{ margin: '0 0 24px 0', color: theme.subtext, fontSize: '14px', lineHeight: '1.5' }}>{badge.desc}</p>
          
          {/* Progress Bar */}
          <div style={{width: '100%', padding: '0 12px', marginBottom: '24px'}}>
             <div style={{display:'flex', justifyContent:'space-between', width:'100%', fontSize:'12px', color: theme.text, marginBottom:'6px', fontWeight: 700}}>
                <span>Progress</span>
                <span>{Math.floor(percent)}%</span>
             </div>
             <div style={{width: '100%', background: '#f1f5f9', height: '12px', borderRadius: '6px', overflow: 'hidden', border: '1px solid #e2e8f0'}}>
                <div style={{width: `${percent}%`, background: badge.unlocked ? theme.primary : theme.secondary, height: '100%', borderRadius: '6px', transition: 'width 0.5s ease-out'}}></div>
             </div>
             <div style={{textAlign: 'right', fontSize: '11px', color: theme.subtext, marginTop: '4px'}}>
                {badge.progress} / {badge.target} completed
             </div>
          </div>

          <StyledButton onClick={() => {
             if(!badge.unlocked && badge.actionTab) {
               onNavigate(badge.actionTab);
               onClose();
             } else {
               onClose();
             }
          }} style={{ 
             background: badge.unlocked ? theme.primary : theme.text,
             boxShadow: badge.unlocked ? '0 4px 12px rgba(21, 128, 61, 0.4)' : 'none'
          }}>
             {badge.unlocked ? 'Equip Badge' : (badge.actionLabel || 'Keep Going!')} 
             {!badge.unlocked && badge.actionTab && <Icons.ChevronRight />}
          </StyledButton>
       </div>
    </div>
  )
};


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

// --- New Navigation Components ---

const Confetti = () => {
  const colors = ['#fbbf24', '#ef4444', '#3b82f6', '#10b981'];
  return (
    <div style={{ position: 'absolute', inset: 0, overflow: 'hidden', pointerEvents: 'none' }}>
      {[...Array(30)].map((_, i) => (
        <div key={i} style={{
          position: 'absolute',
          top: -20,
          left: `${Math.random() * 100}%`,
          width: '10px', height: '10px',
          background: colors[Math.floor(Math.random() * colors.length)],
          animation: `fall ${2 + Math.random() * 3}s linear infinite`,
          animationDelay: `${Math.random() * 2}s`
        }} />
      ))}
    </div>
  )
}

const NavigationMap = () => (
  <div style={{ width: '100%', height: '100%', background: '#e2e8f0', position: 'relative', overflow: 'hidden', perspective: '1000px' }}>
     <div style={{ 
       width: '150%', height: '150%', position: 'absolute', top: '-25%', left: '-25%',
       transform: 'rotateX(45deg) rotateZ(-20deg)', background: '#f1f5f9'
     }}>
        {/* Isometric Grid/Roads */}
        <svg width="100%" height="100%" viewBox="0 0 400 400">
           <defs>
             <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
               <feGaussianBlur stdDeviation="4" result="blur" />
               <feComposite in="SourceGraphic" in2="blur" operator="over" />
             </filter>
           </defs>
           
           {/* Decor - Buildings */}
           <rect x="50" y="50" width="60" height="60" fill="#cbd5e1" stroke="#94a3b8" strokeWidth="2" />
           <rect x="50" y="40" width="60" height="60" fill="#e2e8f0" stroke="#94a3b8" strokeWidth="2" /> {/* Roof */}
           
           <rect x="250" y="200" width="80" height="80" fill="#cbd5e1" stroke="#94a3b8" strokeWidth="2" />
           <rect x="250" y="190" width="80" height="80" fill="#e2e8f0" stroke="#94a3b8" strokeWidth="2" />

           {/* Route Path */}
           <path 
             d="M 200 350 L 200 250 Q 200 150 100 150 L 50 150" 
             stroke="#4ade80" strokeWidth="12" fill="none" strokeLinecap="round" strokeDasharray="10 5"
             filter="url(#glow)"
             style={{ animation: 'dash 20s linear infinite' }}
           />
           
           {/* User Location Marker */}
           <g transform="translate(200, 300)">
              <circle r="12" fill="#3b82f6" fillOpacity="0.3">
                 <animate attributeName="r" values="12;24" dur="1.5s" repeatCount="indefinite" />
                 <animate attributeName="opacity" values="0.3;0" dur="1.5s" repeatCount="indefinite" />
              </circle>
              <circle r="8" fill="#3b82f6" stroke="white" strokeWidth="2" />
           </g>
        </svg>
     </div>
  </div>
);

const DynamicIsland = () => (
  <div style={{
    position: 'absolute', top: '16px', left: '50%', transform: 'translateX(-50%)',
    background: 'black', color: 'white', padding: '12px 24px', borderRadius: '30px',
    display: 'flex', alignItems: 'center', gap: '12px', zIndex: 100,
    boxShadow: '0 4px 20px rgba(0,0,0,0.3)', width: 'auto', minWidth: '280px', justifyContent: 'center'
  }}>
    <div style={{ width: '8px', height: '8px', background: '#ef4444', borderRadius: '50%', animation: 'blink 1s infinite' }} />
    <span style={{ fontSize: '13px', fontWeight: 600 }}>Bus D1 arriving in 2 mins ⚡</span>
  </div>
);

const TransportCard = ({ onFinish }: { onFinish: () => void }) => {
  const [activeMode, setActiveMode] = useState('bus');

  return (
    <div style={{
      position: 'absolute', bottom: '24px', left: '16px', right: '16px',
      background: 'rgba(255,255,255,0.9)', backdropFilter: 'blur(10px)',
      borderRadius: '24px', padding: '20px',
      boxShadow: '0 10px 30px rgba(0,0,0,0.1)',
      display: 'flex', flexDirection: 'column', gap: '16px', zIndex: 100
    }}>
       {/* Mode Switcher */}
       <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', background: '#f1f5f9', padding: '4px', borderRadius: '16px', width: 'fit-content', margin: '0 auto' }}>
          {['🚶', '🚲', '🚌'].map((m, i) => {
             const modes = ['walk', 'cycle', 'bus'];
             const isActive = activeMode === modes[i];
             return (
               <div 
                 key={m} 
                 onClick={() => setActiveMode(modes[i])}
                 style={{ 
                   padding: '8px 16px', borderRadius: '12px', fontSize: '18px', cursor: 'pointer',
                   background: isActive ? 'white' : 'transparent',
                   boxShadow: isActive ? '0 2px 4px rgba(0,0,0,0.05)' : 'none',
                   transition: 'all 0.2s'
                 }}
               >
                 {m}
               </div>
             )
          })}
       </div>

       <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 8px' }}>
          <div>
             <div style={{ fontSize: '12px', color: theme.subtext, fontWeight: 600 }}>EST. ARRIVAL</div>
             <div style={{ fontSize: '24px', fontWeight: 800 }}>10:45 AM</div>
          </div>
          <div style={{ textAlign: 'right' }}>
             <div style={{ fontSize: '12px', color: theme.subtext, fontWeight: 600 }}>DISTANCE</div>
             <div style={{ fontSize: '24px', fontWeight: 800 }}>1.2 <span style={{ fontSize: '14px', color: theme.subtext }}>km</span></div>
          </div>
       </div>

       <StyledButton onClick={onFinish} style={{ background: '#ef4444' }}>
          End Trip
       </StyledButton>
    </div>
  )
}

const CelebrationOverlay = ({ onBack, outfit, newBadge }: { onBack: () => void, outfit: any, newBadge: any }) => (
  <div style={{
    position: 'absolute', inset: 0, zIndex: 200,
    background: 'rgba(255,255,255,0.95)', backdropFilter: 'blur(5px)',
    display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
    animation: 'popIn 0.5s ease-out'
  }}>
    <Confetti />
    
    <div style={{ transform: 'scale(1.5)', marginBottom: '40px' }}>
       <MascotLion outfit={outfit} />
    </div>

    <div style={{ 
      background: 'white', borderRadius: '24px', padding: '32px', width: '85%',
      boxShadow: '0 20px 40px -10px rgba(0,0,0,0.1)', textAlign: 'center',
      border: '1px solid rgba(255,255,255,0.5)',
      position: 'relative', zIndex: 10
    }}>
       <h2 style={{ fontSize: '28px', fontWeight: 800, margin: '0 0 8px 0', color: theme.primary }}>Trip Complete!</h2>
       <p style={{ color: theme.subtext, fontSize: '14px', marginBottom: '24px' }}>Great job reducing your carbon footprint.</p>
       
       <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' }}>
          <div style={{ background: '#ecfdf5', padding: '16px', borderRadius: '16px' }}>
             <div style={{ fontSize: '24px', marginBottom: '4px' }}>🌱</div>
             <div style={{ fontSize: '18px', fontWeight: 700, color: '#166534' }}>+50</div>
             <div style={{ fontSize: '10px', color: '#15803d', fontWeight: 600 }}>Green Points</div>
          </div>
          <div style={{ background: '#eff6ff', padding: '16px', borderRadius: '16px' }}>
             <div style={{ fontSize: '24px', marginBottom: '4px' }}>☁️</div>
             <div style={{ fontSize: '18px', fontWeight: 700, color: '#1e40af' }}>120g</div>
             <div style={{ fontSize: '10px', color: '#1d4ed8', fontWeight: 600 }}>CO2 Saved</div>
          </div>
       </div>

       {newBadge && (
          <div style={{animation: 'popIn 0.5s', background: '#fffbeb', border: '2px solid #f59e0b', padding: '12px', borderRadius: '16px', marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '12px'}}>
             <div style={{fontSize: '32px'}}>{newBadge.icon}</div>
             <div style={{textAlign: 'left'}}>
                <div style={{fontSize: '10px', fontWeight: 800, color: '#b45309', textTransform: 'uppercase'}}>Badge Unlocked!</div>
                <div style={{fontSize: '14px', fontWeight: 700, color: '#92400e'}}>{newBadge.name}</div>
             </div>
          </div>
       )}

       <StyledButton onClick={onBack}>Awesome!</StyledButton>
    </div>
  </div>
);

const RoutesScreen = ({ outfit, onUnlockAchievement }: { outfit?: any, onUnlockAchievement: (id: string) => any }) => {
  const [tripState, setTripState] = useState<'active' | 'finished'>('active');
  const [unlockedBadge, setUnlockedBadge] = useState<any>(null);

  const handleFinish = () => {
    // Simulate unlocking the 'Carbon Zero' badge (ID 4) for the demo
    const badge = onUnlockAchievement('4');
    setUnlockedBadge(badge);
    setTripState('finished');
  };

  return (
    <div style={{ height: '100%', background: theme.bg, display: 'flex', flexDirection: 'column', position: 'relative' }}>
      
      {tripState === 'active' ? (
        <>
          <NavigationMap />
          <DynamicIsland />
          <TransportCard onFinish={handleFinish} />
        </>
      ) : (
        <CelebrationOverlay onBack={() => setTripState('active')} outfit={outfit || { head: 'none', face: 'none', body: 'none' }} newBadge={unlockedBadge} />
      )}

    </div>
  );
};

const ProfileScreen = ({ onNavigate, points, setPoints, outfit, setOutfit, inventory, setInventory, achievements }: any) => {
  const [activeTab, setActiveTab] = useState('closet');
  const [successModal, setSuccessModal] = useState<{open: boolean, msg: string}>({ open: false, msg: '' });
  const [selectedBadge, setSelectedBadge] = useState<any>(null);

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

  const handleBadgeClick = (badge: any) => {
    // If unlocked, allow equip toggle, otherwise show details
    if (badge.unlocked) {
      // We can still show details if clicked, but let's prioritize equipping if it's the main interaction.
      // However, to enrich the activity, let's show modal for all so they can see the 'Equip' button inside the rich modal.
      setSelectedBadge(badge);
    } else {
      setSelectedBadge(badge);
    }
  };

  const handleEquipBadgeFromModal = (badgeId: string) => {
      const isEquipped = outfit.badge === badgeId;
      if (isEquipped) {
        setOutfit({ ...outfit, badge: 'none' });
      } else {
        setOutfit({ ...outfit, badge: badgeId });
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
           
           <div style={{ width: '160px', height: '160px', margin: '0 auto' }}>
             <MascotLion outfit={outfit} />
           </div>
           
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
            {achievements.map((badge: any) => {
              const isEquipped = outfit.badge === badge.id;
              return (
              <div key={badge.id} 
                onClick={() => handleBadgeClick(badge)}
                style={{ 
                  background: 'white', padding: '16px 8px', borderRadius: '16px',
                  display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px',
                  opacity: badge.unlocked ? 1 : 0.6,
                  filter: badge.unlocked ? 'none' : 'grayscale(1)',
                  textAlign: 'center', boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                  border: isEquipped ? `2px solid ${theme.primary}` : '2px solid transparent',
                  cursor: 'pointer',
                  position: 'relative'
                }}
              >
                  {isEquipped && <div style={{ position: 'absolute', top: 8, right: 8, color: theme.primary, fontSize: '12px' }}><Icons.Check /></div>}
                  {!badge.unlocked && <div style={{ position: 'absolute', top: 8, right: 8, color: theme.subtext, fontSize: '12px' }}><Icons.Lock /></div>}
                  
                  <div style={{ fontSize: '32px' }}>{badge.icon}</div>
                  <div style={{ fontWeight: 600, fontSize: '12px' }}>{badge.name}</div>
                  <div style={{ fontSize: '10px', color: theme.subtext }}>{badge.unlocked ? 'Unlocked' : `${badge.progress}/${badge.target}`}</div>
              </div>
            )})}
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
      
      {selectedBadge && (
        <BadgeDetailModal 
          badge={selectedBadge} 
          onClose={() => setSelectedBadge(null)} 
          onNavigate={(tab) => {
             // If navigating to perform action, close modal first
             if(!selectedBadge.unlocked) {
                onNavigate(tab);
                setSelectedBadge(null);
             } else {
                // If equipping, call equip and close
                handleEquipBadgeFromModal(selectedBadge.id);
                setSelectedBadge(null);
             }
          }} 
        />
      )}
    </div>
  );
};

// --- Missing Screen Implementations ---

const LoginScreen = ({ onLogin, onSignUp }: any) => {
  const [loading, setLoading] = useState(false);
  return (
    <div style={{ padding: '40px 24px', display: 'flex', flexDirection: 'column', height: '100%', justifyContent: 'center', alignItems: 'center' }}>
      <div style={{ fontSize: '80px', marginBottom: '20px' }}>🦁</div>
      <h1 style={{ fontSize: '40px', fontWeight: 800, color: theme.primary, marginBottom: '8px' }}>LiNUS</h1>
      <p style={{ color: theme.subtext, marginBottom: '40px' }}>Your Campus Companion</p>
      
      <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <StyledInput placeholder="Student ID" />
        <StyledInput placeholder="Password" type="password" />
        <StyledButton onClick={() => { setLoading(true); setTimeout(onLogin, 1000); }}>
          {loading ? <Spinner /> : 'Log In'}
        </StyledButton>
      </div>

      <p style={{ marginTop: '24px', fontSize: '14px', color: theme.subtext }}>
        Don't have an account? <span onClick={onSignUp} style={{ color: theme.primary, fontWeight: 'bold', cursor: 'pointer' }}>Sign Up</span>
      </p>
    </div>
  );
};

const SignupWizard = ({ onComplete }: any) => {
  const [step, setStep] = useState(0);
  const [selectedFaculty, setSelectedFaculty] = useState<any>(null);

  if (step === 0) {
    return (
       <div style={{ padding: '24px', height: '100%', display: 'flex', flexDirection: 'column' }}>
          <h2 style={{ fontSize: '28px', fontWeight: 800 }}>Choose Faculty</h2>
          <p style={{ color: theme.subtext, marginBottom: '24px' }}>This determines your starter outfit!</p>
          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '12px' }}>
             {FACULTIES.map(f => (
                <div key={f.id} onClick={() => setSelectedFaculty(f)}
                  style={{ 
                    padding: '16px', borderRadius: '16px', 
                    border: selectedFaculty?.id === f.id ? `2px solid ${theme.primary}` : `2px solid ${theme.border}`,
                    background: selectedFaculty?.id === f.id ? '#f0fdf4' : 'white',
                    display: 'flex', alignItems: 'center', gap: '16px', cursor: 'pointer'
                  }}>
                   <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: f.color }} />
                   <div>
                      <div style={{ fontWeight: 700 }}>{f.name}</div>
                      <div style={{ fontSize: '12px', color: theme.subtext }}>{f.slogan}</div>
                   </div>
                </div>
             ))}
          </div>
          <StyledButton disabled={!selectedFaculty} onClick={() => setStep(1)}>Continue</StyledButton>
       </div>
    )
  }

  return (
    <div style={{ padding: '24px', height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center' }}>
       <h2 style={{ fontSize: '28px', fontWeight: 800 }}>You're all set!</h2>
       <p style={{ color: theme.subtext, marginBottom: '40px' }}>Meet your new buddy.</p>
       
       <div style={{ width: '200px', height: '200px', marginBottom: '40px' }}>
          <MascotLion outfit={selectedFaculty.outfit} />
       </div>

       <StyledButton onClick={() => onComplete(selectedFaculty)}>Let's Go!</StyledButton>
    </div>
  )
};

const Onboarding = ({ onComplete }: any) => {
  useEffect(() => { onComplete() }, []);
  return null;
};

const HomeScreen = ({ onOpenMap, onOpenActivities, onOpenProfile }: any) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
         <div>
            <h1 style={{ fontSize: '24px', fontWeight: 800, margin: 0 }}>Hi, Alex! 👋</h1>
            <p style={{ color: theme.subtext, margin: 0, fontSize: '14px' }}>Let's explore campus.</p>
         </div>
         <div onClick={onOpenProfile} style={{ width: '40px', height: '40px', borderRadius: '50%', overflow: 'hidden', background: '#f1f5f9' }}>
            <MascotLion outfit={{ head: 'none', face: 'none', body: 'shirt_nus' }} />
         </div>
      </div>

      <div style={{ background: theme.primary, borderRadius: '24px', padding: '24px', color: 'white', position: 'relative', overflow: 'hidden' }}>
         <div style={{ position: 'relative', zIndex: 1 }}>
            <div style={{ fontSize: '12px', fontWeight: 600, opacity: 0.8 }}>NEARBY BUS</div>
            <div style={{ fontSize: '36px', fontWeight: 800, margin: '4px 0' }}>D1 <span style={{ fontSize: '18px', fontWeight: 500 }}>to UTown</span></div>
            <div style={{ display: 'inline-block', background: 'rgba(255,255,255,0.2)', padding: '4px 12px', borderRadius: '12px', fontSize: '12px', fontWeight: 600 }}>Arriving in 2 min</div>
         </div>
         <div style={{ position: 'absolute', right: -20, bottom: -20, fontSize: '120px', opacity: 0.2 }}>🚌</div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
         <div onClick={onOpenMap} style={{ background: 'white', padding: '20px', borderRadius: '20px', boxShadow: '0 4px 10px rgba(0,0,0,0.05)', cursor: 'pointer' }}>
            <div style={{ width: '40px', height: '40px', background: '#eff6ff', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#3b82f6', marginBottom: '12px' }}><Icons.MapPin /></div>
            <div style={{ fontWeight: 700 }}>Campus Map</div>
         </div>
         <div onClick={onOpenActivities} style={{ background: 'white', padding: '20px', borderRadius: '20px', boxShadow: '0 4px 10px rgba(0,0,0,0.05)', cursor: 'pointer' }}>
            <div style={{ width: '40px', height: '40px', background: '#f0fdf4', borderRadius: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#16a34a', marginBottom: '12px' }}><Icons.Leaf /></div>
            <div style={{ fontWeight: 700 }}>Activities</div>
         </div>
      </div>
      
      <div style={{ background: 'white', padding: '20px', borderRadius: '20px', boxShadow: '0 4px 10px rgba(0,0,0,0.05)' }}>
         <h3 style={{ margin: '0 0 16px 0', fontSize: '16px' }}>Community Leaderboard</h3>
         {COMMUNITIES.slice(0, 3).map((c, i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', alignItems: 'center' }}>
               <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <div style={{ width: '24px', height: '24px', borderRadius: '50%', background: i===0 ? '#fef08a' : '#f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', fontWeight: 700 }}>{c.rank}</div>
                  <div style={{ fontSize: '14px', fontWeight: 600 }}>{c.name}</div>
               </div>
               <div style={{ fontWeight: 700 }}>{c.score}</div>
            </div>
         ))}
      </div>
    </div>
  )
};

const MapScreen = ({ onBack }: any) => (
  <div style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
     <div style={{ paddingBottom: '16px', display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div onClick={onBack} style={{ cursor: 'pointer', padding: '8px', background: 'white', borderRadius: '50%' }}><Icons.ArrowLeft /></div>
        <h2 style={{ margin: 0 }}>Map</h2>
     </div>
     <div style={{ flex: 1, borderRadius: '24px', overflow: 'hidden', border: `1px solid ${theme.border}` }}>
        <CartoonMap onPinClick={(f: any) => alert(f.name)} />
     </div>
  </div>
);

const CommunityScreen = () => (
  <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
     <h2 style={{ margin: 0, fontSize: '28px', fontWeight: 800 }}>Community</h2>
     <div style={{ display: 'flex', gap: '16px', overflowX: 'auto' }}>
        <div style={{ minWidth: '150px', background: theme.primary, color: 'white', padding: '20px', borderRadius: '20px' }}>
           <div style={{ fontSize: '12px', opacity: 0.8 }}>YOUR RANK</div>
           <div style={{ fontSize: '32px', fontWeight: 800 }}>#42</div>
           <div style={{ fontSize: '12px' }}>Top 5%</div>
        </div>
         <div style={{ minWidth: '150px', background: 'white', padding: '20px', borderRadius: '20px', boxShadow: '0 4px 10px rgba(0,0,0,0.05)' }}>
           <div style={{ fontSize: '12px', color: theme.subtext }}>TOTAL POINTS</div>
           <div style={{ fontSize: '32px', fontWeight: 800 }}>1,250</div>
        </div>
     </div>
     <div style={{ background: 'white', borderRadius: '24px', padding: '24px' }}>
        <h3 style={{ margin: '0 0 20px 0' }}>Faculty Rankings</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
           {COMMUNITIES.map((c, i) => (
              <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                 <div style={{ width: '20px', fontWeight: 700, color: theme.subtext }}>{c.rank}</div>
                 <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                       <span style={{ fontWeight: 600, fontSize: '14px' }}>{c.name}</span>
                       <span style={{ fontWeight: 700, fontSize: '14px' }}>{c.score}</span>
                    </div>
                    <div style={{ width: '100%', height: '8px', background: '#f1f5f9', borderRadius: '4px', overflow: 'hidden' }}>
                       <div style={{ width: `${(c.score/5000)*100}%`, height: '100%', background: c.color }} />
                    </div>
                 </div>
              </div>
           ))}
        </div>
     </div>
  </div>
);

const ChatScreen = () => {
  const [messages, setMessages] = useState<any[]>([{ role: 'model', text: "Hi! I'm LiNUS. Ask me about bus routes, food, or events!" }]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);
  const chatRef = useRef<Chat | null>(null);

  useEffect(() => {
    try {
      if (process.env.API_KEY) {
        const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
        chatRef.current = ai.chats.create({
          model: 'gemini-3-flash-preview',
          config: { systemInstruction: "You are LiNUS, a helpful NUS mascot." }
        });
      }
    } catch (e) { console.error(e); }
  }, []);

  useEffect(() => { scrollRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || loading) return;
    const text = input;
    setInput('');
    setMessages(prev => [...prev, { role: 'user', text }]);
    setLoading(true);

    try {
      if (chatRef.current) {
         const result = await chatRef.current.sendMessage({ message: text });
         setMessages(prev => [...prev, { role: 'model', text: result.text || "" }]);
      } else {
         // Fallback if no API key
         setTimeout(() => {
            setMessages(prev => [...prev, { role: 'model', text: "I can't connect to my brain right now (API Key missing), but I'm listening! 🦁" }]);
            setLoading(false);
         }, 1000);
         return;
      }
    } catch (e) {
      setMessages(prev => [...prev, { role: 'model', text: "Oops, something went wrong. Try again!" }]);
    }
    setLoading(false);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
       <div style={{ flex: 1, overflowY: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {messages.map((m, i) => (
             <div key={i} style={{ alignSelf: m.role === 'user' ? 'flex-end' : 'flex-start', maxWidth: '80%' }}>
                <div style={{ 
                  background: m.role === 'user' ? theme.primary : 'white', 
                  color: m.role === 'user' ? 'white' : theme.text,
                  padding: '12px 16px', borderRadius: '16px',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                }}>
                   {m.text}
                </div>
             </div>
          ))}
          {loading && <div style={{ alignSelf: 'flex-start' }}><Spinner /></div>}
          <div ref={scrollRef} />
       </div>
       <div style={{ padding: '16px', background: 'white', borderTop: `1px solid ${theme.border}`, display: 'flex', gap: '8px' }}>
          <input 
            value={input} onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSend()}
            placeholder="Ask LiNUS..."
            style={{ flex: 1, border: `1px solid ${theme.border}`, borderRadius: '24px', padding: '12px 16px', outline: 'none' }}
          />
          <button onClick={handleSend} style={{ width: '40px', height: '40px', borderRadius: '50%', background: theme.primary, color: 'white', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Icons.Send /></button>
       </div>
    </div>
  );
};

const VoucherScreen = ({ onBack, points, setPoints }: any) => (
  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', height: '100%' }}>
     <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div onClick={onBack} style={{ cursor: 'pointer', padding: '8px', background: 'white', borderRadius: '50%' }}><Icons.ArrowLeft /></div>
        <h2 style={{ margin: 0 }}>Vouchers</h2>
     </div>
     <div style={{ background: theme.primary, padding: '24px', borderRadius: '24px', color: 'white' }}>
        <div style={{ fontSize: '12px', opacity: 0.8 }}>BALANCE</div>
        <div style={{ fontSize: '32px', fontWeight: 800 }}>{points} pts</div>
     </div>
     <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', overflowY: 'auto' }}>
        {VOUCHERS.map(v => (
           <div key={v.id} style={{ background: 'white', padding: '16px', borderRadius: '16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ fontSize: '32px' }}>{v.icon}</div>
              <div style={{ fontWeight: 700, fontSize: '14px' }}>{v.name}</div>
              <StyledButton disabled={points < v.cost} onClick={() => {
                 if(points >= v.cost) { setPoints(points - v.cost); alert('Redeemed!'); }
              }} style={{ fontSize: '12px', padding: '8px', marginTop: 'auto' }}>{v.cost} pts</StyledButton>
           </div>
        ))}
     </div>
  </div>
);

const ActivityScreen = ({ onBack }: any) => (
  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', height: '100%' }}>
     <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div onClick={onBack} style={{ cursor: 'pointer', padding: '8px', background: 'white', borderRadius: '50%' }}><Icons.ArrowLeft /></div>
        <h2 style={{ margin: 0 }}>Activities</h2>
     </div>
     <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {ACTIVITIES.map(a => (
           <div key={a.id} style={{ background: 'white', padding: '16px', borderRadius: '20px', display: 'flex', gap: '16px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
              <div style={{ width: '60px', height: '60px', borderRadius: '16px', background: a.imageColor, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '24px' }}>{a.icon}</div>
              <div>
                 <div style={{ fontSize: '10px', fontWeight: 700, color: a.imageColor, textTransform: 'uppercase' }}>{a.category}</div>
                 <div style={{ fontWeight: 700, fontSize: '16px' }}>{a.title}</div>
                 <div style={{ fontSize: '12px', color: theme.subtext }}>{a.desc}</div>
              </div>
           </div>
        ))}
     </div>
  </div>
);

const SettingsScreen = ({ onBack, onLogout }: any) => (
  <div style={{ padding: '24px' }}>
     <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '32px' }}>
        <div onClick={onBack} style={{ cursor: 'pointer', padding: '8px', background: 'white', borderRadius: '50%' }}><Icons.ArrowLeft /></div>
        <h2 style={{ margin: 0 }}>Settings</h2>
     </div>
     <div style={{ background: 'white', borderRadius: '16px', overflow: 'hidden', marginBottom: '32px' }}>
        {['Account', 'Notifications', 'Privacy', 'About'].map((s, i) => (
           <div key={s} style={{ padding: '16px', borderBottom: i<3 ? `1px solid ${theme.border}` : 'none', display: 'flex', justifyContent: 'space-between' }}>
              {s} <Icons.ChevronRight />
           </div>
        ))}
     </div>
     <StyledButton onClick={onLogout} style={{ background: '#ef4444' }}>Log Out</StyledButton>
  </div>
);

const AboutScreen = ({ onBack }: any) => (
  <div style={{ padding: '24px', textAlign: 'center' }}>
     <div onClick={onBack} style={{ position: 'absolute', top: 24, left: 24 }}><Icons.ArrowLeft /></div>
     <h1 style={{ marginTop: '60px' }}>LiNUS</h1>
     <p>Version 1.0.0</p>
  </div>
);

// --- Mobile Wrapper & App ---

const MobileApp = () => {
  const [activeTab, setActiveTab] = useState('home');
  const [appState, setAppState] = useState<'login' | 'onboarding' | 'signup' | 'app'>('login');
  
  // Lifted State
  const [points, setPoints] = useState(1250);
  const [outfit, setOutfit] = useState({ head: 'none', face: 'none', body: 'none', badge: 'none' });
  const [inventory, setInventory] = useState<string[]>([]); 
  const [achievements, setAchievements] = useState(INITIAL_ACHIEVEMENTS);

  const handleLogin = () => setAppState('app');
  const handleSignUp = () => setAppState('signup');
  const handleOnboardingComplete = () => setAppState('app');
  const handleLogout = () => {
    setAppState('login');
    setActiveTab('home');
  };

  const handleSignupComplete = (facultyData: any) => {
    const newItems = [facultyData.outfit.head, facultyData.outfit.body, facultyData.outfit.face].filter(i => i !== 'none');
    setInventory(prev => [...prev, ...newItems]);
    setOutfit({ ...facultyData.outfit, badge: 'none' });
    setAppState('app');
  };

  // Logic to unlock achievement and return the unlocked badge object for display
  const handleUnlockAchievement = (id: string) => {
      let unlockedBadge = null;
      setAchievements(prev => prev.map(a => {
         if (a.id === id && !a.unlocked) {
            unlockedBadge = { ...a, unlocked: true, progress: a.target }; // Max out progress
            return unlockedBadge;
         } else if (a.id === id) {
            return a; // Already unlocked, do nothing
         }
         return a;
      }));
      return unlockedBadge;
  };

  const renderScreen = () => {
    if (appState === 'login') return <LoginScreen onLogin={handleLogin} onSignUp={handleSignUp} />;
    if (appState === 'signup') return <SignupWizard onComplete={handleSignupComplete} />;
    if (appState === 'onboarding') return <Onboarding onComplete={handleOnboardingComplete} />;

    switch(activeTab) {
      case 'home': return <HomeScreen onOpenMap={() => setActiveTab('map')} onOpenActivities={() => setActiveTab('activities')} onOpenProfile={() => setActiveTab('profile')} />;
      case 'map': return <MapScreen onBack={() => setActiveTab('home')} />;
      case 'routes': return <RoutesScreen outfit={outfit} onUnlockAchievement={handleUnlockAchievement} />;
      case 'community': return <CommunityScreen />;
      case 'chat': return <ChatScreen />;
      case 'profile': return <ProfileScreen onNavigate={(screen: string) => setActiveTab(screen)} points={points} setPoints={setPoints} outfit={outfit} setOutfit={setOutfit} inventory={inventory} setInventory={setInventory} achievements={achievements} />;
      case 'vouchers': return <VoucherScreen onBack={() => setActiveTab('profile')} points={points} setPoints={setPoints} />;
      case 'activities': return <ActivityScreen onBack={() => setActiveTab('home')} />;
      case 'settings': return <SettingsScreen onBack={() => setActiveTab('profile')} onLogout={handleLogout} />;
      case 'about': return <AboutScreen onBack={() => setActiveTab('profile')} />;
      default: return <HomeScreen onOpenMap={() => setActiveTab('map')} onOpenActivities={() => setActiveTab('activities')} onOpenProfile={() => setActiveTab('profile')} />;
    }
  };

  const showNavBar = appState === 'app' && ['home', 'routes', 'community', 'chat', 'profile'].includes(activeTab);

  return (
    <div style={mobileStyles.phoneContainer}>
      <style>{animationStyles}</style>
      <div style={mobileStyles.notch} />
      <div style={mobileStyles.statusBar}>
        <span>9:41</span>
        <div style={{ display: 'flex', gap: '4px' }}>
          <Icons.Wifi />
          <span>100%</span>
        </div>
      </div>

      <div style={{ ...mobileStyles.content, paddingBottom: showNavBar ? '100px' : '20px' }}>
        {renderScreen()}
      </div>

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