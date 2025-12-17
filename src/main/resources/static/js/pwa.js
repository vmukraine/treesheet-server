var pwaUtil = {
  requestNotificationPermission: function() {
    if (Notification.permission !== 'granted') {
      Notification.requestPermission();
    }
  },
  
  sendTestPictureNotification: function() {
    if (Notification.permission === 'granted') {
      var f = function(req) {
        if (req) {
          req.showNotification('Hello from PWA!', {
            body: 'This is a notification with a picture.',
            //icon: '/images/icon_128.png', // Small icon
            //image: '/images/test_512x256.png', // Large image
            tag: 'picture-notification'
          });
        }
      };
      navigator.serviceWorker.getRegistration().then(f);
    } else {
      alert('Could not send notification due to missing permission');
    }
  }
};