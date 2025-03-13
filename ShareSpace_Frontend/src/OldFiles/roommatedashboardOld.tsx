import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { BookingRequest, Room } from '../types';
import { api } from '../api';
import toast from 'react-hot-toast';

export default function BookingForm() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [room, setRoom] = useState<Room | null>(null);
  const [formData, setFormData] = useState<BookingRequest>({
    username: '',
    password: '',
    email: '',
    gender: '',
    withFood: false,
    checkInDate: '',
    checkOutDate: '',
    referralId: ''
  });

  useEffect(() => {
    // Fetch room details
    const fetchRoom = async () => {
      if (!roomId) return;
      try {
        const response = await api.getRoom(roomId);
        setRoom(response.data);
      } catch (error) {
        toast.error('Error fetching room details');
        navigate('/');
      }
    };

    fetchRoom();
  }, [roomId, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roomId || !room) return;

    setLoading(true);
    try {
      // Validate form data before proceeding to booking
      if (!formData.username || !formData.email || !formData.gender || !formData.checkInDate) {
        throw new Error('Please fill all required fields');
      }

      // Directly book the room
      await api.bookRoom(roomId!, formData);
      toast.success('Room booked successfully!');
      navigate('/');
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Failed to process booking');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }));
  };

  if (!room) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-[#1a1a1a]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#1a1a1a]">
      <div className="p-4 md:p-8 ml-0">
        <div className="max-w-2xl mx-auto bg-[#242424] rounded-xl shadow-xl overflow-hidden">
          <div className="p-6 md:p-8">
            <h1 className="text-2xl md:text-3xl font-bold mb-6 md:mb-8 text-purple-400 text-center">
              Book Room
            </h1>
            
            {/* Room Summary */}
            <div className="mb-8 p-4 md:p-6 bg-[#2f2f2f] rounded-lg">
              <h2 className="text-xl md:text-2xl font-bold mb-2 text-purple-300">{room.roomType} Room</h2>
              <p className="text-lg md:text-xl text-purple-400">Monthly Rent: ₹{room.price}</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-base md:text-lg font-medium text-purple-300 mb-2">Username</label>
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  className="w-full p-4 rounded-lg bg-[#2f2f2f] text-base md:text-lg text-purple-200 border-none 
                    focus:ring-2 focus:ring-purple-500 placeholder-purple-300/50"
                  placeholder="Enter your username"
                />
              </div>
              
              <div>
                <label className="block text-base md:text-lg font-medium text-purple-300 mb-2">Password</label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className="w-full p-4 rounded-lg bg-[#2f2f2f] text-base md:text-lg text-purple-200 border-none 
                    focus:ring-2 focus:ring-purple-500 placeholder-purple-300/50"
                  placeholder="Enter your password"
                />
              </div>

              <div>
                <label className="block text-base md:text-lg font-medium text-purple-300 mb-2">Email</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  className="w-full p-4 rounded-lg bg-[#2f2f2f] text-base md:text-lg text-purple-200 border-none 
                    focus:ring-2 focus:ring-purple-500 placeholder-purple-300/50"
                  placeholder="Enter your email"
                />
              </div>

              <div>
                <label className="block text-base md:text-lg font-medium text-purple-300 mb-2">Gender</label>
                <select
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  required
                  className="w-full p-4 rounded-lg bg-[#2f2f2f] text-base md:text-lg text-purple-200 border-none 
                    focus:ring-2 focus:ring-purple-500"
                >
                  <option value="" className="text-gray-400">Select gender</option>
                  <option value="male" className="text-purple-200">Male</option>
                  <option value="female" className="text-purple-200">Female</option>
                  <option value="other" className="text-purple-200">Other</option>
                </select>
              </div>

              <div className="flex items-center p-4 rounded-lg bg-[#2f2f2f]">
                <input
                  type="checkbox"
                  name="withFood"
                  checked={formData.withFood}
                  onChange={handleChange}
                  className="w-5 h-5 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
                />
                <label className="ml-3 text-base md:text-lg font-medium text-purple-300">
                  Include Food Service
                </label>
              </div>

              <div>
                <label className="block text-base md:text-lg font-medium text-purple-300 mb-2">Check-in Date</label>
                <input
                  type="date"
                  name="checkInDate"
                  value={formData.checkInDate}
                  onChange={handleChange}
                  required
                  className="w-full p-4 rounded-lg bg-[#2f2f2f] text-base md:text-lg text-purple-200 border-none 
                    focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div>
                <label className="block text-base md:text-lg font-medium text-purple-300 mb-2">Check-out Date</label>
                <input
                  type="date"
                  name="checkOutDate"
                  value={formData.checkOutDate}
                  onChange={handleChange}
                  className="w-full p-4 rounded-lg bg-[#2f2f2f] text-base md:text-lg text-purple-200 border-none 
                    focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div>
                <label className="block text-base md:text-lg font-medium text-purple-300 mb-2">Referral Id</label>
                <input
                  type="text"
                  name="referralId"
                  value={formData.referralId}
                  onChange={handleChange}
                  className="w-full p-4 rounded-lg bg-[#2f2f2f] text-base md:text-lg text-purple-200 border-none 
                    focus:ring-2 focus:ring-purple-500 placeholder-purple-300/50"
                  placeholder="Enter referral ID (optional)"
                />
              </div>

              <div className="flex gap-4">
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-gradient-to-r from-purple-600 to-purple-500 text-white 
                    py-4 px-6 rounded-lg font-semibold shadow-lg 
                    hover:shadow-purple-500/50 transform hover:-translate-y-0.5 
                    transition-all duration-200 text-base md:text-lg
                    disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Processing...' : 'Book Now'}
                </button>
                <button
                  type="button"
                  onClick={() => navigate(-1)}
                  className="flex-1 bg-[#2f2f2f] text-purple-300 
                    py-4 px-6 rounded-lg font-semibold
                    hover:bg-[#3f3f3f] transition-all duration-200 
                    text-base md:text-lg"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}