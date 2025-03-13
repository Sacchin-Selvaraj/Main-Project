import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Home, Users, User, Sun, Moon } from 'lucide-react';

interface SidebarProps {
  isDarkMode: boolean;
  toggleTheme: () => void;
}

export default function Sidebar({ isDarkMode, toggleTheme }: SidebarProps) {
  const navigate = useNavigate();
  const [isVisible, setIsVisible] = useState(false);

  return (
    <div className="relative">
      {/* Trigger button with enhanced glass effect */}
      <button 
        className={`fixed left-0 top-1/2 -translate-y-1/2 
          w-6 md:w-8 h-12 md:h-20 
          backdrop-blur-lg bg-[#242424]/80
          rounded-r-xl flex items-center justify-center z-40 
          hover:bg-purple-600/20 transition-all duration-300
          border-r border-purple-500/20 hover:border-purple-500/40
          group ${isVisible ? 'opacity-0' : 'opacity-100'}`}
        onMouseEnter={() => setIsVisible(true)}
        onClick={() => setIsVisible(true)}
      >
        <span className="flex flex-col gap-1.5">
          <span className="w-1 md:w-1.5 h-1 md:h-1.5 bg-purple-400 rounded-full 
            group-hover:scale-110 transition-transform duration-300"></span>
          <span className="w-1 md:w-1.5 h-1 md:h-1.5 bg-purple-400 rounded-full 
            group-hover:scale-110 transition-transform duration-300 delay-75"></span>
          <span className="w-1 md:w-1.5 h-1 md:h-1.5 bg-purple-400 rounded-full 
            group-hover:scale-110 transition-transform duration-300 delay-150"></span>
        </span>
      </button>

      {/* Overlay with blur effect */}
      {isVisible && (
        <div 
          className="fixed inset-0 bg-black/60 backdrop-blur-sm z-30 md:hidden"
          onClick={() => setIsVisible(false)}
        />
      )}

      {/* Sidebar with enhanced glass effect */}
      <div 
        className={`fixed left-0 top-0 h-full transition-all duration-300 ease-in-out
          backdrop-blur-lg bg-[#242424]/95 
          border-r border-purple-500/20
          z-50 ${isVisible 
            ? 'w-[240px] md:w-72 opacity-100 translate-x-0 pointer-events-auto' 
            : 'w-0 opacity-0 -translate-x-full pointer-events-none'}`}
        onMouseLeave={() => setIsVisible(false)}
      >
        <div className={`p-4 md:p-6 min-w-[240px] md:min-w-[288px] ${
          isVisible ? 'pointer-events-auto' : 'pointer-events-none'
        }`}>
          {/* Close button with animation */}
          <button
            className="absolute top-4 right-4 p-2 text-purple-400 hover:text-purple-300 
              transition-all duration-300 hover:rotate-90 md:hidden"
            onClick={() => setIsVisible(false)}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>

          {/* Logo section with enhanced gradient */}
          <div className="mb-8 pb-6 border-b border-purple-500/20">
            <span className="text-2xl md:text-3xl font-bold text-transparent bg-clip-text 
              bg-gradient-to-r from-purple-400 to-purple-600 tracking-wider">
              ShareSpace
            </span>
          </div>

          {/* Navigation menu with enhanced hover effects */}
          <nav className={`space-y-2 md:space-y-3 ${
            isVisible ? 'pointer-events-auto' : 'pointer-events-none'
          }`}>
            <button
              onClick={() => {
                navigate('/');
                setIsVisible(false);
              }}
              className="w-full flex items-center gap-3 p-3 rounded-xl 
                bg-transparent hover:bg-purple-600/10
                text-purple-300 hover:text-purple-400
                transition-all duration-300 group
                border border-transparent hover:border-purple-500/20"
            >
              <Home className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />
              <span className="text-base font-medium">Home</span>
            </button>

            <button
              onClick={() => {
                navigate('/owner/login');
                setIsVisible(false);
              }}
              className="w-full flex items-center gap-3 p-3 rounded-xl 
                bg-transparent hover:bg-purple-600/10
                text-purple-300 hover:text-purple-400
                transition-all duration-300 group
                border border-transparent hover:border-purple-500/20"
            >
              <Users className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />
              <span className="text-base font-medium">Owner Dashboard</span>
            </button>

            <button
              onClick={() => {
                navigate('/roommate');
                setIsVisible(false);
              }}
              className="w-full flex items-center gap-3 p-3 rounded-xl 
                bg-transparent hover:bg-purple-600/10
                text-purple-300 hover:text-purple-400
                transition-all duration-300 group
                border border-transparent hover:border-purple-500/20"
            >
              <User className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />
              <span className="text-base font-medium">Roommate Dashboard</span>
            </button>

            <button
              onClick={() => {
                toggleTheme();
                setIsVisible(false);
              }}
              className="w-full flex items-center gap-3 p-3 rounded-xl 
                bg-transparent hover:bg-purple-600/10
                text-purple-300 hover:text-purple-400
                transition-all duration-300 group
                border border-transparent hover:border-purple-500/20"
            >
              {isDarkMode ? 
                <Sun className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" /> : 
                <Moon className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />
              }
              <span className="text-base font-medium">Toggle Theme</span>
            </button>
          </nav>
        </div>
      </div>
    </div>
  );
}