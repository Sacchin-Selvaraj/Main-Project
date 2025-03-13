import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Key, User, Lock } from 'lucide-react';
import toast from 'react-hot-toast';
import { api } from '../api';

export default function OwnerLogin() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [credentials, setCredentials] = useState({
    ownerName: '',
    password: ''
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await api.verifyOwner(credentials);
      if (response.data) {
        toast.success('Login successful!');
        navigate('/owner/dashboard', { replace: true });
      } else {
        toast.error('Invalid credentials');
      }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Invalid Credentials';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-[#1a1a1a] to-[#2d1f3f] flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl shadow-2xl 
          border border-purple-500/20 overflow-hidden transform hover:scale-[1.01] 
          transition-all duration-300">
          <div className="p-8 md:p-10">
            {/* Header Section */}
            <div className="text-center mb-8">
              <div className="inline-block p-3 rounded-full bg-purple-500/10 mb-4">
                <Key className="w-10 h-10 text-purple-400" />
              </div>
              <h2 className="text-3xl md:text-4xl font-bold text-transparent bg-clip-text 
                bg-gradient-to-r from-purple-400 to-purple-600 mb-2">
                Owner Login
              </h2>
              <p className="text-purple-300/60 text-sm md:text-base">
                Access your dashboard securely
              </p>
            </div>

            {/* Form Section */}
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                {/* Username Field */}
                <div>
                  <label className="block text-sm md:text-base font-medium mb-2 text-purple-300">
                    Username
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <User className="h-5 w-5 text-purple-400/60" />
                    </div>
                    <input
                      type="text"
                      value={credentials.ownerName}
                      onChange={(e) => setCredentials(prev => ({ ...prev, ownerName: e.target.value }))}
                      className="w-full pl-12 pr-4 py-4 rounded-xl bg-[#2f2f2f]/50 text-base md:text-lg 
                        text-purple-200 border border-purple-500/20 focus:border-purple-500/50
                        focus:ring-2 focus:ring-purple-500/20 placeholder-purple-300/30
                        transition-all duration-200"
                      placeholder="Enter your username"
                      required
                      disabled={loading}
                    />
                  </div>
                </div>

                {/* Password Field */}
                <div>
                  <label className="block text-sm md:text-base font-medium mb-2 text-purple-300">
                    Password
                  </label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                      <Lock className="h-5 w-5 text-purple-400/60" />
                    </div>
                    <input
                      type="password"
                      value={credentials.password}
                      onChange={(e) => setCredentials(prev => ({ ...prev, password: e.target.value }))}
                      className="w-full pl-12 pr-4 py-4 rounded-xl bg-[#2f2f2f]/50 text-base md:text-lg 
                        text-purple-200 border border-purple-500/20 focus:border-purple-500/50
                        focus:ring-2 focus:ring-purple-500/20 placeholder-purple-300/30
                        transition-all duration-200"
                      placeholder="Enter your password"
                      required
                      disabled={loading}
                    />
                  </div>
                </div>
              </div>

              {/* Buttons Section */}
              <div className="space-y-4 pt-2">
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-gradient-to-r from-purple-600 to-purple-500 text-white 
                    py-4 px-6 rounded-xl font-semibold shadow-lg 
                    hover:shadow-purple-500/25 transform hover:-translate-y-0.5 
                    transition-all duration-200 text-base md:text-lg
                    disabled:opacity-50 disabled:cursor-not-allowed
                    disabled:hover:transform-none"
                >
                  {loading ? (
                    <div className="flex items-center justify-center">
                      <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" 
                        xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" 
                          stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" 
                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                        </path>
                      </svg>
                      Logging in...
                    </div>
                  ) : 'Login to Dashboard'}
                </button>
                
                <button
                  type="button"
                  onClick={() => navigate('/')}
                  className="w-full bg-[#2f2f2f]/50 text-purple-300 
                    py-4 px-6 rounded-xl font-semibold
                    hover:bg-[#2f2f2f]/70 transition-all duration-200 
                    text-base md:text-lg border border-purple-500/20
                    hover:border-purple-500/40"
                >
                  Back to Home
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}