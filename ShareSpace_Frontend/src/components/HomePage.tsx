import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import { AvailabilityRequest, Room } from '../types';
import { Building, Wifi, Shield, Utensils, Car, Trees as Tree } from 'lucide-react';
import toast from 'react-hot-toast';

export default function HomePage() {
  const navigate = useNavigate();
  const [availabilityRequest, setAvailabilityRequest] = useState<AvailabilityRequest>({
    roomType: '',
    withAC: false,
    withFood: false,
    capacity: 1
  });
  const [matchingRooms, setMatchingRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(false);
  const [navigating, setNavigating] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await api.checkAvailability(availabilityRequest);
      setMatchingRooms(response.data);
      if (response.data.length === 0) {
        toast.error('No rooms available with these criteria');
      } else {
        toast.success(`Found ${response.data.length} matching rooms!`);
      }
    } catch (error: any) {
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error('Error checking room availability');
      }
      setMatchingRooms([]);
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetails = useCallback((roomId: string) => {
    if (navigating) return;
    setNavigating(true);
    setTimeout(() => {
      navigate(`/room/${roomId}`);
    }, 100);
  }, [navigate, navigating]);

  // Reset navigating state when component unmounts
  useEffect(() => {
    return () => {
      setNavigating(false);
    };
  }, []);

  const amenities = [
    { icon: <Building className="w-6 h-6" />, text: 'Furnished rooms with bed, mattress, and wardrobe' },
    { icon: <Wifi className="w-6 h-6" />, text: 'Wi-Fi internet connection' },
    { icon: <Shield className="w-6 h-6" />, text: 'Security features (CCTV, security guard)' },
    { icon: <Utensils className="w-6 h-6" />, text: 'Three meals per day (breakfast, lunch, dinner)' },
    { icon: <Car className="w-6 h-6" />, text: 'Parking space for bikes/cars' },
    { icon: <Tree className="w-6 h-6" />, text: 'Garden/outdoor sitting area' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#1a1a1a] via-[#2d1f3f] to-[#1a1a1a] p-6 md:p-10">
      <div className="max-w-[1400px] mx-auto">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Left Column */}
          <div className="space-y-8">
            {/* Check Availability Form */}
            <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8
              border border-purple-500/20 shadow-lg shadow-purple-500/5">
              <h2 className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-purple-400 
                to-purple-600 bg-clip-text text-transparent mb-8 text-center">
                Check Room Availability
              </h2>
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Room Type Select */}
                <div>
                  <select
                    value={availabilityRequest.roomType}
                    onChange={(e) => setAvailabilityRequest(prev => ({ ...prev, roomType: e.target.value }))}
                    className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                      text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40"
                    required
                  >
                    <option value="" className="bg-[#2f2f2f] text-purple-300">Select Room Type</option>
                    <option value="Single sharing" className="bg-[#2f2f2f] text-purple-300">Single</option>
                    <option value="Two sharing" className="bg-[#2f2f2f] text-purple-300">Double</option>
                    <option value="Three sharing" className="bg-[#2f2f2f] text-purple-300">Three</option>
                  </select>
                </div>

                {/* Toggle Switches */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {/* AC Room Toggle */}
                  <div className="bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-4
                    border border-purple-500/20 flex items-center justify-between">
                    <span className="text-purple-300 font-medium">AC Room</span>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={availabilityRequest.withAC}
                        onChange={(e) => setAvailabilityRequest(prev => ({ ...prev, withAC: e.target.checked }))}
                        className="sr-only peer"
                      />
                      <div className="w-11 h-6 bg-[#2f2f2f] peer-focus:ring-4 peer-focus:ring-purple-500/40 
                        rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white 
                        after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-purple-400 
                        after:border-purple-400 after:border after:rounded-full after:h-5 after:w-5 
                        after:transition-all peer-checked:bg-purple-600/20"></div>
                    </label>
                  </div>

                  {/* Food Service Toggle */}
                  <div className="bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-4
                    border border-purple-500/20 flex items-center justify-between">
                    <span className="text-purple-300 font-medium">Food Service</span>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={availabilityRequest.withFood}
                        onChange={(e) => setAvailabilityRequest(prev => ({ ...prev, withFood: e.target.checked }))}
                        className="sr-only peer"
                      />
                      <div className="w-11 h-6 bg-[#2f2f2f] peer-focus:ring-4 peer-focus:ring-purple-500/40 
                        rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white 
                        after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-purple-400 
                        after:border-purple-400 after:border after:rounded-full after:h-5 after:w-5 
                        after:transition-all peer-checked:bg-purple-600/20"></div>
                    </label>
                  </div>
                </div>

                {/* Capacity Input */}
                <div>
                  <label className="block text-sm font-medium text-purple-300/60 mb-2">Capacity</label>
                  <input
                    type="number"
                    min="1"
                    value={availabilityRequest.capacity}
                    onChange={(e) => setAvailabilityRequest(prev => ({ ...prev, capacity: parseInt(e.target.value) }))}
                    className="w-full rounded-xl bg-[#2f2f2f]/50 border border-purple-500/20 
                      text-purple-300 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/40
                      [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none 
                      [&::-webkit-inner-spin-button]:appearance-none"
                    required
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full flex items-center justify-center gap-3 px-6 py-4 
                    bg-gradient-to-r from-purple-600 to-purple-500 
                    hover:from-purple-700 hover:to-purple-600
                    text-white rounded-xl transition-all duration-300
                    disabled:opacity-50 disabled:cursor-not-allowed font-medium
                    transform hover:scale-[1.02] active:scale-[0.98]"
                >
                  {loading ? (
                    <div className="flex items-center gap-3">
                      <div className="w-5 h-5 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                      <span className="text-lg">Checking...</span>
                    </div>
                  ) : (
                    <span className="text-lg">Check Availability</span>
                  )}
                </button>
              </form>
            </div>

            {/* Matching Rooms Section */}
            {matchingRooms.length > 0 && (
              <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8
                border border-purple-500/20 shadow-lg shadow-purple-500/5">
                <h3 className="text-2xl font-bold bg-gradient-to-r from-purple-400 
                  to-purple-600 bg-clip-text text-transparent mb-6">
                  Available Rooms
                </h3>
                <div className="space-y-4">
                  {matchingRooms.map((room) => (
                    <div
                      key={room.roomId}
                      className="group bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                        hover:bg-purple-600/10 transition-all duration-300 cursor-pointer
                        border border-purple-500/20 hover:border-purple-500/40
                        transform hover:-translate-y-1 hover:shadow-xl hover:shadow-purple-500/10"
                    >
                      <div className="flex justify-between items-start">
                        <div className="space-y-4">
                          <div className="flex items-center gap-3">
                            <div className="p-2 rounded-lg bg-purple-500/10 group-hover:bg-purple-500/20
                              transition-all duration-300">
                              <Building className="w-6 h-6 text-purple-400" />
                            </div>
                            <h4 className="text-xl font-semibold text-purple-300">
                              {room.roomType}
                            </h4>
                          </div>
                          <div className="flex gap-2">
                            <span className={`px-3 py-1 rounded-xl text-sm font-medium
                              ${room.isAcAvailable 
                                ? 'bg-green-500/10 text-green-400 border border-green-500/20' 
                                : 'bg-gray-500/10 text-gray-400 border border-gray-500/20'}`}>
                              {room.isAcAvailable ? 'AC Room' : 'Non-AC Room'}
                            </span>
                            <span className={`px-3 py-1 rounded-xl text-sm font-medium
                              ${room.withFood 
                                ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                                : 'bg-gray-500/10 text-gray-400 border border-gray-500/20'}`}>
                              {room.withFood ? 'Food Included' : 'Food Optional'}
                            </span>
                          </div>
                          <div className="space-y-2 text-purple-200/80">
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Capacity: {room.capacity} person(s)
                            </p>
                            <p className="flex items-center gap-2">
                              <span className="w-2 h-2 rounded-full bg-purple-400/60"></span>
                              Current Occupancy: {room.currentCapacity} person(s)
                            </p>
                            <p className="text-lg font-semibold text-purple-400">
                              ₹{room.price}/month
                            </p>
                          </div>
                        </div>
                        <button
                          type="button"
                          disabled={navigating}
                          onClick={() => handleViewDetails(room.roomId)}
                          className="px-6 py-3 bg-gradient-to-r from-purple-600/10 to-purple-500/10 
                            hover:from-purple-600/20 hover:to-purple-500/20 text-purple-400 
                            rounded-xl transition-all duration-300 border border-purple-500/20 
                            hover:border-purple-500/40 transform hover:scale-[1.02]
                            disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {navigating ? 'Loading...' : 'View Details'}
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Right Column - Amenities Section */}
          <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl p-8
            border border-purple-500/20 shadow-lg shadow-purple-500/5">
            <h2 className="text-2xl md:text-3xl font-bold bg-gradient-to-r from-purple-400 
              to-purple-600 bg-clip-text text-transparent mb-8 text-center">
              PG Amenities
            </h2>
            <div className="grid grid-cols-1 gap-4">
              {amenities.map((amenity, index) => (
                <div 
                  key={index} 
                  className="group bg-[#2f2f2f]/50 backdrop-blur-sm rounded-xl p-6
                    hover:bg-purple-600/10 transition-all duration-300
                    border border-purple-500/20 hover:border-purple-500/40
                    flex items-center gap-4"
                >
                  <div className="p-3 rounded-lg bg-purple-500/10 group-hover:bg-purple-500/20
                    transition-all duration-300 transform group-hover:scale-110">
                    {React.cloneElement(amenity.icon as React.ReactElement, {
                      className: "w-6 h-6 text-purple-400"
                    })}
                  </div>
                  <span className="text-purple-200/80 font-medium">{amenity.text}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}