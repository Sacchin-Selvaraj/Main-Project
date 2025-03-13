interface Razorpay {
    new (options: any): {
      open: () => void;
    };
  }
  
  interface Window {
    Razorpay: Razorpay;
  }