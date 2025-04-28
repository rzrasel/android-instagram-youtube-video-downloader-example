from flask import Flask, request, jsonify
from instaloader import Instaloader, Post
from urllib.parse import urlparse

app = Flask(__name__)
loader = Instaloader()

@app.route('/extract-video-url', methods=['POST'])
def extract_video_url():
    data = request.get_json()
    url = data.get('url')
    if not url or 'instagram.com' not in url:
        return jsonify({'error': 'Invalid Instagram URL'}), 400

    try:
        # Extract shortcode from URL
        path = urlparse(url).path.strip('/')
        shortcode = path.split('/')[-1] if path.split('/')[-1] else path.split('/')[-2]
        post = Post.from_shortcode(loader.context, shortcode)
        if post.is_video:
            return jsonify({'videoUrl': post.video_url})
        return jsonify({'error': 'No video found in the post'}), 404
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)