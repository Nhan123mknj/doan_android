package com.example.newsapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newsapp.Model.Comments;
import com.example.newsapp.Model.Reply;
import com.example.newsapp.Repository.CommentRepository;

import java.util.List;

public class CommentViewModel extends ViewModel{
    CommentRepository commentRepository;
    private MutableLiveData<List<Comments>> commentsLiveData;
    private MutableLiveData<String> errorLiveData;

    public CommentViewModel(){
        commentRepository = new CommentRepository();
        commentsLiveData = commentRepository.getCommentsLiveData();
        errorLiveData = commentRepository.getErrorLiveData();
    }
    public LiveData<List<Comments>> getCommentsByArticleId(String articleId){
        return commentRepository.getCommentsByArticleId(articleId);
    }
    public void addComment(String articleId, Comments comment){
        commentRepository.addComment(articleId, comment);
    }

    public void addReply(String articleId, String commentId, Reply reply){
        commentRepository.addReply(articleId, commentId, reply);
    }

    public void updateComment(String articleId, String commentId, String newContent) {
        commentRepository.updateComment(articleId, commentId, newContent);
    }

    public void deleteComment(String articleId, String commentId) {
        commentRepository.deleteComment(articleId, commentId);
    }

    public void updateReply(String articleId, String commentId, String replyId, String newContent) {
        commentRepository.updateReply(articleId, commentId, replyId, newContent);
    }

    public void deleteReply(String articleId, String commentId, String replyId) {
        commentRepository.deleteReply(articleId, commentId, replyId);
    }

    public MutableLiveData<List<Comments>> getCommentsLiveData() {
        return commentsLiveData;
    }

}
