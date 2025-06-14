const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyFollowersOnNewArticle = functions.firestore
    .document('articles/{articleId}')
    .onCreate(async (snap, context) => {
        const article = snap.data();
        const authorId = article.author;

        // Lấy danh sách follower
        const followsSnap = await admin.firestore()
            .collection('follows')
            .where('authorId', '==', authorId)
            .get();

        if (followsSnap.empty) return null;

        // Lấy FCM token của từng follower
        const tokens = [];
        for (const doc of followsSnap.docs) {
            const followerId = doc.data().followerId;
            const userSnap = await admin.firestore().collection('users').doc(followerId).get();
            if (userSnap.exists && userSnap.data().fcmToken) {
                tokens.push(userSnap.data().fcmToken);
            }
        }

        if (tokens.length === 0) return null;

        // Gửi thông báo
        const payload = {
            notification: {
                title: 'Tác giả bạn theo dõi vừa đăng bài mới!',
                body: article.title || 'Có bài viết mới, hãy xem ngay!',
            }
        };
        return admin.messaging().sendToDevice(tokens, payload);
    });