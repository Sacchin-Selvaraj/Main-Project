import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { Roommate, RazorpayResponse, LoginRequest } from '../types';
import { User, Calendar, Mail, Home, Users, Utensils, Edit, LogOut, AlertTriangle, MessageSquare, CreditCard } from 'lucide-react';
import toast from 'react-hot-toast';

declare global {
  interface Window {
    Razorpay: Razorpay;
  }
}

export default function RoommateDashboard() {
  const navigate = useNavigate();
  const [roommate, setRoommate] = useState<Roommate | null>(null);
  const [loading, setLoading] = useState(false);
  const [loginRequest, setLoginRequest] = useState<LoginRequest>({
    username: '',
    password: ''
  });
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  
  // Modal states  
  const [showEditModal, setShowEditModal] = useState(false);
  const [showVacateModal, setShowVacateModal] = useState(false);
  const [showGrievanceModal, setShowGrievanceModal] = useState(false);
  
  // Form states
  const [editForm, setEditForm] = useState({
    username: '',
    password: '',
    email: '',
    withFood: false,
    checkOutDate: ''
  });
  
  const [vacateForm, setVacateForm] = useState({
    vacateReason: '',
    checkOutDate: ''
  });
  
  const [grievanceForm, setGrievanceForm] = useState({
    grievanceContent: '',
    grievanceFrom: new Date().toISOString().split('T')[0]
  });

  useEffect(() => {
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    document.body.appendChild(script);
    
    return () => {
      document.body.removeChild(script);
    };
  }, []);

  useEffect(() => {
    if (roommate) {
      setEditForm({
        username: roommate.username,
        password: '',
        email: roommate.email,
        withFood: roommate.withFood,
        checkOutDate: roommate.checkOutDate || ''
      });
    }
  }, [roommate]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await api.getRoommate(loginRequest);
      setRoommate(response.data);
      setIsLoggedIn(true);
      toast.success('Login successful!');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Login failed. Please check your credentials.';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handlePayRent = async () => {
    if (!roommate) return;
    setLoading(true);
    try {
      const { data: order } = await api.payRent(roommate.username);

      const options = {
        key: 'rzp_test_nxog52ig2XH5qP',
        amount: order.amount * 100,
        currency: 'INR',
        name: 'Roommate Payment',
        description: 'PG Rent Payment',
        order_id: order.orderId,
        handler: async (response: RazorpayResponse) => {
          const paymentData = {
            orderId: response.razorpay_order_id,
            paymentId: response.razorpay_payment_id,
            email: order.email
          };

          try {
            const callbackResponse = await api.createPaymentCallback(paymentData);
            if (callbackResponse.status === 200) {
              toast.success('Rent payment successful!');
              // Refresh roommate data to update payment status
              const updatedRoommate = await api.getRoommate(loginRequest);
              setRoommate(updatedRoommate.data);
            } else {
              toast.error('Failed to confirm payment. Please contact support.');
            }
          } catch (error: any) {
            if (error.response?.data?.message) {
              toast.error(error.response.data.message);
            } else {
              toast.error('Error processing payment callback');
            }
          }
        },
        prefill: {
          name: roommate.username,
          email: roommate.email,
        },
        theme: {
          color: '#3399cc',
        },
      };

      const razorpay = new window.Razorpay(options);
      razorpay.open();
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to process rent payment';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roommate) return;
    setLoading(true);
    try {
      const response = await api.updateRoommateDetails(roommate.roommateId, editForm);
      setRoommate(response.data);
      setLoginRequest(prev => ({ ...prev, username: response.data.username }));
      setShowEditModal(false);
      toast.success('Profile updated successfully!');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to update profile';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleVacateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roommate) return;
    setLoading(true);
    try {
      await api.sendVacateRequest(roommate.roommateId, vacateForm);
      setShowVacateModal(false);
      toast.success('Vacate request sent successfully!');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to send vacate request';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleGrievanceSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roommate) return;
    setLoading(true);
    try {
      await api.raiseGrievance(roommate.roommateId, grievanceForm);
      setShowGrievanceModal(false);
      toast.success('Grievance submitted successfully!');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to submit grievance';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setRoommate(null);
    navigate('/');
  };

  const handleCopyReferralId = () => {
    if (roommate?.referralId) {
      navigator.clipboard.writeText(roommate.referralId);
      toast.success('Referral ID copied to clipboard!');
    }
  };

  if (!isLoggedIn) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#1a1a1a] via-[#2d1f3f] to-[#1a1a1a] 
        flex items-center justify-center p-4">
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8 
          border border-purple-500/20 shadow-lg shadow-purple-500/5 w-96">
          <h2 className="text-2xl font-bold bg-gradient-to-r from-purple-400 to-purple-600 
            bg-clip-text text-transparent mb-6 text-center">
            Roommate Login
          </h2>
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-purple-300/60">Username</label>
              <input
                type="text"
                value={loginRequest.username}
                onChange={(e) => setLoginRequest(prev => ({...prev, username: e.target.value}))}
                className="mt-1 block w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                  text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40
                  placeholder-purple-300/40"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-purple-300/60">Password</label>
              <input
                type="password"
                value={loginRequest.password}
                onChange={(e) => setLoginRequest(prev => ({...prev, password: e.target.value}))}
                className="mt-1 block w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                  text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40
                  placeholder-purple-300/40"
                required
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gradient-to-r from-purple-600 to-purple-500 text-white py-3 px-4 
                rounded-xl hover:from-purple-700 hover:to-purple-600 transition-all duration-300
                disabled:opacity-50 disabled:cursor-not-allowed font-medium"
            >
              {loading ? (
                <div className="flex items-center justify-center gap-2">
                  <div className="w-5 h-5 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                  <span>Logging in...</span>
                </div>
              ) : (
                'Login'
              )}
            </button>
          </form>
        </div>
      </div>
    );
  }

  if (!roommate) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#1a1a1a] via-[#2d1f3f] to-[#1a1a1a] 
        flex items-center justify-center p-4">
        <div className="w-16 h-16 border-4 border-purple-500/20 border-t-purple-500 
          rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#1a1a1a] via-[#2d1f3f] to-[#1a1a1a]">
      <div className="p-6 md:p-10 max-w-[1400px] mx-auto">
        {/* Header Section */}
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8 mb-8
          border border-purple-500/20 shadow-lg shadow-purple-500/5">
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
            <div className="space-y-2">
              <h1 className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-purple-400 to-purple-600 
                bg-clip-text text-transparent">
                Roommate Dashboard
              </h1>
              <p className="text-purple-300/60 text-base">
                Manage your stay and payments
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-4">
              <button
                onClick={() => setShowEditModal(true)}
                className="group flex items-center gap-2 px-6 py-3.5 
                  bg-gradient-to-r from-purple-600/10 to-purple-500/10 
                  hover:from-purple-600/20 hover:to-purple-500/20
                  text-purple-400 rounded-xl transition-all duration-300
                  border border-purple-500/20 hover:border-purple-500/40"
              >
                <Edit className="w-5 h-5" />
                <span className="text-base font-medium">Edit Profile</span>
              </button>
              <button
                onClick={() => setShowGrievanceModal(true)}
                className="group flex items-center gap-2 px-6 py-3.5 
                  bg-gradient-to-r from-yellow-600/10 to-yellow-500/10 
                  hover:from-yellow-600/20 hover:to-yellow-500/20
                  text-yellow-400 rounded-xl transition-all duration-300
                  border border-yellow-500/20 hover:border-yellow-500/40"
              >
                <MessageSquare className="w-5 h-5" />
                <span className="text-base font-medium">Raise Grievance</span>
              </button>
              <button
                onClick={() => setShowVacateModal(true)}
                className="group flex items-center gap-2 px-6 py-3.5 
                  bg-gradient-to-r from-red-600/10 to-red-500/10 
                  hover:from-red-600/20 hover:to-red-500/20
                  text-red-400 rounded-xl transition-all duration-300
                  border border-red-500/20 hover:border-red-500/40"
              >
                <LogOut className="w-5 h-5" />
                <span className="text-base font-medium">Request Vacate</span>
              </button>
              <button
                onClick={handleLogout}
                className="group flex items-center gap-2 px-6 py-3.5 
                  bg-gradient-to-r from-gray-600/10 to-gray-500/10 
                  hover:from-gray-600/20 hover:to-gray-500/20
                  text-gray-400 rounded-xl transition-all duration-300
                  border border-gray-500/20 hover:border-gray-500/40"
              >
                <LogOut className="w-5 h-5" />
                <span className="text-base font-medium">Logout</span>
              </button>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Profile Card */}
          <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8
            border border-purple-500/20 shadow-lg shadow-purple-500/5">
            <div className="flex items-center gap-4 mb-6">
              <div className="p-3 rounded-xl bg-purple-500/10">
                <User className="w-8 h-8 text-purple-400" />
              </div>
              <div>
                <h2 className="text-xl font-semibold text-purple-300">{roommate.username}</h2>
                <p className="text-purple-300/60">{roommate.email}</p>
              </div>
            </div>
            
            <div className="space-y-4">
              {[
                { icon: Home, label: 'Room Number', value: roommate.roomNumber },
                { icon: Users, label: 'Gender', value: roommate.gender },
                { icon: Utensils, label: 'Food', value: roommate.withFood ? 'Included' : 'Not Included' },
                { icon: Calendar, label: 'Check In', value: roommate.checkInDate },
                ...(roommate.checkOutDate ? [{ icon: Calendar, label: 'Check Out', value: roommate.checkOutDate }] : [])
              ].map(({ icon: Icon, label, value }) => (
                <div key={label} className="flex items-center gap-3 text-purple-200/80">
                  <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                  <Icon className="w-5 h-5 text-purple-400" />
                  <span className="font-medium">{label}:</span>
                  <span className="ml-auto">{value}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Payment History Card */}
          <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8
            border border-purple-500/20 shadow-lg shadow-purple-500/5">
            <div className="flex items-center gap-3 mb-8">
              <div className="p-2 rounded-lg bg-purple-500/10">
                <CreditCard className="w-6 h-6 text-purple-400" />
              </div>
              <h2 className="text-xl font-bold bg-gradient-to-r from-purple-400 to-purple-600 
                bg-clip-text text-transparent">
                Payment History
              </h2>
            </div>
            <div className="space-y-8">
              <div className="bg-[#2f2f2f]/50 rounded-xl p-6 border border-purple-500/20">
                <h3 className="text-lg font-semibold text-transparent bg-clip-text 
                  bg-gradient-to-r from-purple-400 to-purple-600 mb-4">
                  Current Rent Status
                </h3>
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-purple-300/80 font-medium">Amount Due</span>
                    <span className="text-xl font-bold text-purple-300">₹{roommate.rentAmount}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-purple-300/80 font-medium">Status</span>
                    <span className={`px-4 py-1.5 rounded-xl text-sm font-semibold 
                      transform transition-all duration-300 hover:scale-105 ${
                      roommate.rentStatus === 'PAYMENT_DONE' 
                        ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                        : 'bg-red-500/10 text-red-400 border border-red-500/20'
                    }`}>
                      {roommate.rentStatus}
                    </span>
                  </div>
                </div>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-transparent bg-clip-text 
                  bg-gradient-to-r from-purple-400 to-purple-600 mb-6">
                  Recent Transactions
                </h3>
                <div className="space-y-4 max-h-[400px] overflow-y-auto pr-4 
                  [&::-webkit-scrollbar]:w-2
                  [&::-webkit-scrollbar-track]:bg-[#2f2f2f]/30
                  [&::-webkit-scrollbar-track]:rounded-full
                  [&::-webkit-scrollbar-thumb]:bg-purple-500/20
                  [&::-webkit-scrollbar-thumb]:rounded-full
                  [&::-webkit-scrollbar-thumb]:border-4
                  [&::-webkit-scrollbar-thumb]:border-transparent
                  [&::-webkit-scrollbar-thumb]:bg-clip-padding
                  [&::-webkit-scrollbar-thumb]:hover:bg-purple-500/40">
                  {roommate.paymentList && roommate.paymentList.length > 0 ? (
                    roommate.paymentList.map((payment) => (
                      <div
                        key={payment.transactionId}
                        className="bg-[#2f2f2f]/50 rounded-xl p-6 border border-purple-500/20
                          hover:border-purple-500/40 transition-all duration-300
                          hover:shadow-lg hover:shadow-purple-500/5"
                      >
                        <div className="flex items-center justify-between mb-4 flex-wrap gap-2">
                          <div className="flex items-center gap-3">
                            <div className="p-2 rounded-lg bg-purple-500/10">
                              <CreditCard className="w-5 h-5 text-purple-400" />
                            </div>
                            <div>
                              <div className="text-sm font-semibold text-purple-300">
                                Transaction ID
                              </div>
                              <div className="text-xs font-mono text-purple-300/60">
                                {payment.transactionId}
                              </div>
                            </div>
                          </div>
                          <span className={`px-3 py-1 rounded-xl text-sm font-semibold whitespace-nowrap
                            transform transition-all duration-300 ${
                            payment.paymentStatus === 'PAYMENT_DONE' 
                              ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                              : 'bg-red-500/10 text-red-400 border border-red-500/20'
                          }`}>
                            {payment.paymentStatus}
                          </span>
                        </div>
                        
                        <div className="grid grid-cols-2 gap-4">
                          <div className="space-y-1">
                            <div className="text-xs text-purple-300/60">Amount</div>
                            <div className="text-lg font-bold text-purple-300">
                              ₹{payment.amount}
                            </div>
                          </div>
                          
                          <div className="space-y-1">
                            <div className="text-xs text-purple-300/60">Payment Method</div>
                            <div className="text-sm font-medium text-purple-300">
                              {payment.paymentMethod}
                            </div>
                          </div>
                          
                          <div className="space-y-1">
                            <div className="text-xs text-purple-300/60">Date</div>
                            <div className="text-sm font-medium text-purple-300">
                              {payment.paymentDate}
                            </div>
                          </div>
                          
                          <div className="space-y-1">
                            <div className="text-xs text-purple-300/60">Paid By</div>
                            <div className="text-sm font-medium text-purple-300">
                              {roommate.username}
                            </div>
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center py-8 text-purple-300/60 bg-[#2f2f2f]/50 
                      rounded-xl border border-purple-500/20">
                      <p className="font-medium">No payment history available</p>
                      <p className="text-sm mt-1">Your recent transactions will appear here</p>
                    </div>
                  )}
                </div>
              </div>

              {roommate.rentStatus !== 'PAYMENT_DONE' && (
                <button
                  onClick={handlePayRent}
                  disabled={loading}
                  className="w-full flex items-center justify-center gap-3 px-6 py-4 
                    bg-gradient-to-r from-purple-600 to-purple-500 
                    hover:from-purple-700 hover:to-purple-600
                    text-white rounded-xl transition-all duration-300
                    disabled:opacity-50 disabled:cursor-not-allowed font-medium
                    transform hover:scale-[1.02] active:scale-[0.98]"
                >
                  <CreditCard className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
                  <span className="text-lg">{loading ? 'Processing Payment...' : 'Pay Now'}</span>
                </button>
              )}
            </div>
          </div>

          {/* Referrals Card */}
          <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8
            border border-purple-500/20 shadow-lg shadow-purple-500/5">
            <h2 className="text-xl font-bold bg-gradient-to-r from-purple-400 to-purple-600 
              bg-clip-text text-transparent mb-6">
              Referral Program
            </h2>
            <div className="space-y-6">
              <div className="space-y-3">
                <div className="flex justify-between items-center text-purple-200/80">
                  <span>Referral ID</span>
                  <div className="flex items-center gap-2">
                    <span>{roommate.referralId}</span>
                    <button
                      onClick={handleCopyReferralId}
                      className="p-2 rounded-lg bg-purple-500/10 hover:bg-purple-500/20 
                        transition-all duration-200"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-purple-400" 
                        viewBox="0 0 24 24" fill="none" stroke="currentColor" 
                        strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                      </svg>
                    </button>
                  </div>
                </div>
                <div className="flex justify-between items-center text-purple-200/80">
                  <span>Total Referrals</span>
                  <span>{roommate.referralCount}</span>
                </div>
              </div>

              <div>
                <h3 className="text-lg font-medium text-purple-300 mb-4">Recent Referrals</h3>
                <div className="space-y-4">
                  {roommate.referralDetailsList && roommate.referralDetailsList.length > 0 ? (
                    roommate.referralDetailsList.map((referral, index) => (
                      <div key={index} className="border-b border-purple-500/20 pb-4">
                        <div className="flex justify-between items-center">
                          <span className="text-purple-200/80">{referral.username}</span>
                          <span className="text-sm text-purple-300/60">
                            {referral.referralDate}
                          </span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <p className="text-purple-300/60 text-center">No referrals yet</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Modals */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-md w-full border border-purple-500/20 shadow-2xl">
            <h2 className="text-xl font-bold text-transparent bg-clip-text 
              bg-gradient-to-r from-purple-400 to-purple-600 mb-6">
              Edit Profile
            </h2>
            <form onSubmit={handleEditSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">Username</label>
                <input
                  type="text"
                  value={editForm.username}
                  onChange={(e) => setEditForm(prev => ({...prev, username: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">New Password</label>
                <input
                  type="password"
                  value={editForm.password}
                  onChange={(e) => setEditForm(prev => ({...prev, password: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                  placeholder="Leave blank to keep current password"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">Email</label>
                <input
                  type="email"
                  value={editForm.email}
                  onChange={(e) => setEditForm(prev => ({...prev, email: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                  required
                />
              </div>
              <div className="flex items-center">
                <input
                  type="checkbox"
                  checked={editForm.withFood}
                  onChange={(e) => setEditForm(prev => ({...prev, withFood: e.target.checked}))}
                  className="rounded border-purple-500/20 text-purple-600 
                    focus:ring-purple-500/40 bg-[#2f2f2f]/50"
                />
                <label className="ml-2 text-sm font-medium text-purple-300/60">
                  Include Food Service
                </label>
              </div>
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">Check Out Date</label>
                <input
                  type="date"
                  value={editForm.checkOutDate}
                  onChange={(e) => setEditForm(prev => ({...prev, checkOutDate: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                />
              </div>
              <div className="flex justify-end gap-4 mt-6">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="px-6 py-3 bg-[#2f2f2f]/50 text-purple-300 
                    rounded-xl hover:bg-purple-600/20 transition-all duration-200
                    border border-purple-500/20 hover:border-purple-500/40"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-3 bg-gradient-to-r from-purple-600 to-purple-500 
                    text-white rounded-xl hover:from-purple-700 hover:to-purple-600 
                    transition-all duration-300 disabled:opacity-50"
                >
                  {loading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showVacateModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-md w-full border border-purple-500/20 shadow-2xl">
            <h2 className="text-xl font-bold text-transparent bg-clip-text 
              bg-gradient-to-r from-purple-400 to-purple-600 mb-6">
              Submit Vacate Request
            </h2>
            <form onSubmit={handleVacateSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">Reason for Vacating</label>
                <textarea
                  value={vacateForm.vacateReason}
                  onChange={(e) => setVacateForm(prev => ({...prev, vacateReason: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                  rows={4}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">Planned Check-out Date</label>
                <input
                  type="date"
                  value={vacateForm.checkOutDate}
                  onChange={(e) => setVacateForm(prev => ({...prev, checkOutDate: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                  required
                />
              </div>
              <div className="flex justify-end gap-4 mt-6">
                <button
                  type="button"
                  onClick={() => setShowVacateModal(false)}
                  className="px-6 py-3 bg-[#2f2f2f]/50 text-purple-300 
                    rounded-xl hover:bg-purple-600/20 transition-all duration-200
                    border border-purple-500/20 hover:border-purple-500/40"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-3 bg-gradient-to-r from-purple-600 to-purple-500 
                    text-white rounded-xl hover:from-purple-700 hover:to-purple-600 
                    transition-all duration-300 disabled:opacity-50"
                >
                  {loading ? 'Submitting...' : 'Submit Request'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showGrievanceModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-[#242424]/90 backdrop-blur-lg rounded-2xl p-8 
            max-w-md w-full border border-purple-500/20 shadow-2xl">
            <h2 className="text-xl font-bold text-transparent bg-clip-text 
              bg-gradient-to-r from-purple-400 to-purple-600 mb-6">
              Submit Grievance
            </h2>
            <form onSubmit={handleGrievanceSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">Grievance Details</label>
                <textarea
                  value={grievanceForm.grievanceContent}
                  onChange={(e) => setGrievanceForm(prev => ({...prev, grievanceContent: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                  rows={4}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-purple-300/60 mb-2">Date</label>
                <input
                  type="date"
                  value={grievanceForm.grievanceFrom}
                  onChange={(e) => setGrievanceForm(prev => ({...prev, grievanceFrom: e.target.value}))}
                  className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                    text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                  required
                />
              </div>
              <div className="flex justify-end gap-4 mt-6">
                <button
                  type="button"
                  onClick={() => setShowGrievanceModal(false)}
                  className="px-6 py-3 bg-[#2f2f2f]/50 text-purple-300 
                    rounded-xl hover:bg-purple-600/20 transition-all duration-200
                    border border-purple-500/20 hover:border-purple-500/40"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-6 py-3 bg-gradient-to-r from-purple-600 to-purple-500 
                    text-white rounded-xl hover:from-purple-700 hover:to-purple-600 
                    transition-all duration-300 disabled:opacity-50"
                >
                  {loading ? 'Submitting...' : 'Submit Grievance'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
