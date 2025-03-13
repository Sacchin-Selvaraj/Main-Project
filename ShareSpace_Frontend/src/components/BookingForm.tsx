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
  const isMounted = React.useRef(false);
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
      if (!roomId || isMounted.current) return;
      try {
        const response = await api.getRoom(roomId);
        setRoom(response.data);
        isMounted.current = true;
      } catch (error: any) {
        const errorMessage = error.response?.data?.message || 
                           error.message || 
                           'Failed to get room details';
        toast.error(errorMessage);
        navigate('/');
      }
    };

    fetchRoom();

    return () => {
      isMounted.current = false;
    };
  }, [roomId, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roomId || !room) return;
    if (loading) return; // Prevent duplicate submissions while loading

    setLoading(true);
    try {
      if (!formData.username || !formData.email || !formData.gender || !formData.checkInDate) {
        throw new Error('Please fill all required fields');
      }

      await api.bookRoom(roomId!, formData);
      toast.success('Room booked successfully!');
      navigate('/');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 
                         error.message || 
                         'Failed to process booking';
      toast.error(errorMessage);
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
        <div className="w-12 h-12 border-4 border-purple-500/20 border-t-purple-500 
          rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#1a1a1a] py-8 md:py-12">
      <div className="max-w-3xl mx-auto px-4 md:px-8">
        <div className="bg-[#242424]/80 backdrop-blur-lg rounded-2xl 
          shadow-xl overflow-hidden border border-purple-500/20
          hover:border-purple-500/30 transition-all duration-300">
          <div className="p-6 md:p-8">
            <h1 className="text-2xl md:text-3xl font-bold mb-8 
              text-transparent bg-clip-text bg-gradient-to-r 
              from-purple-400 to-purple-600 text-center">
              Book Room
            </h1>
            
            {/* Room Summary */}
            <div className="mb-8 p-6 rounded-xl bg-[#2f2f2f]/50 
              border border-purple-500/20 hover:border-purple-500/40 
              transition-all duration-300 group">
              <h2 className="text-xl md:text-2xl font-bold mb-2 
                text-transparent bg-clip-text bg-gradient-to-r 
                from-purple-300 to-purple-500">
                {room.roomType} Room
              </h2>
              <p className="text-lg md:text-xl text-purple-400 group-hover:text-purple-300 
                transition-colors duration-300">
                Monthly Rent: ₹{room.price}
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Form Fields */}
              {[
                { label: 'Username', name: 'username', type: 'text' },
                { label: 'Password', name: 'password', type: 'password' },
                { label: 'Email', name: 'email', type: 'email' },
                { label: 'Check-in Date', name: 'checkInDate', type: 'date' },
                { label: 'Check-out Date', name: 'checkOutDate', type: 'date', optional: true },
                { label: 'Referral Id', name: 'referralId', type: 'text', optional: true }
              ].map((field) => (
                <div key={field.name} className="group">
                  <label className="block text-base md:text-lg font-medium mb-2
                    text-transparent bg-clip-text bg-gradient-to-r 
                    from-purple-300 to-purple-500">
                    {field.label}
                  </label>
                  <input
                    type={field.type}
                    name={field.name}
                    value={String(formData[field.name as keyof BookingRequest])}
                    onChange={handleChange}
                    required={!field.optional}
                    className="w-full p-4 rounded-xl bg-[#2f2f2f]/50 
                      text-base md:text-lg text-purple-200 
                      border border-purple-500/20 focus:border-purple-500/40
                      focus:ring-2 focus:ring-purple-500/20 
                      placeholder-purple-300/50
                      transition-all duration-300"
                    placeholder={`Enter your ${field.label.toLowerCase()}`}
                  />
                </div>
              ))}

              {/* Gender Selection */}
              <div className="group">
                <label className="block text-base md:text-lg font-medium mb-2
                  text-transparent bg-clip-text bg-gradient-to-r 
                  from-purple-300 to-purple-500">
                  Gender
                </label>
                <select
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  required
                  className="w-full p-4 rounded-xl bg-[#2f2f2f]/50 
                    text-base md:text-lg text-purple-200 
                    border border-purple-500/20 focus:border-purple-500/40
                    focus:ring-2 focus:ring-purple-500/20 
                    transition-all duration-300"
                >
                  <option value="" className="text-purple-300/50">Select gender</option>
                  <option value="Male" className="text-purple-200">Male</option>
                  <option value="Transgender" className="text-purple-200">Transgender</option>
                </select>
              </div>

              {/* Food Service Checkbox */}
              <div className="flex items-center p-4 rounded-xl bg-[#2f2f2f]/50 
                border border-purple-500/20 hover:border-purple-500/40 
                transition-all duration-300 group">
                <input
                  type="checkbox"
                  name="withFood"
                  checked={formData.withFood}
                  onChange={handleChange}
                  className="w-5 h-5 text-purple-600 bg-[#2f2f2f] 
                    border-purple-500/40 rounded 
                    focus:ring-purple-500/20 focus:ring-2"
                />
                <label className="ml-3 text-base md:text-lg font-medium 
                  text-purple-300 group-hover:text-purple-200 
                  transition-colors duration-300">
                  Include Food Service
                </label>
              </div>

              {/* Action Buttons */}
              <div className="flex gap-4">
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 bg-gradient-to-r from-purple-600 to-purple-500 
                    text-white py-4 px-6 rounded-xl font-semibold 
                    hover:from-purple-500 hover:to-purple-400
                    transform hover:scale-[1.02] active:scale-[0.98]
                    transition-all duration-300 shadow-lg 
                    hover:shadow-purple-500/25 
                    disabled:opacity-50 disabled:cursor-not-allowed
                    text-base md:text-lg"
                >
                  {loading ? 'Processing...' : 'Book Now'}
                </button>
                <button
                  type="button"
                  onClick={() => navigate(-1)}
                  className="flex-1 bg-[#2f2f2f]/50 text-purple-300 
                    py-4 px-6 rounded-xl font-semibold
                    hover:bg-[#2f2f2f]/70 hover:text-purple-200
                    transform hover:scale-[1.02] active:scale-[0.98]
                    transition-all duration-300 border border-purple-500/20 
                    hover:border-purple-500/40 text-base md:text-lg"
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