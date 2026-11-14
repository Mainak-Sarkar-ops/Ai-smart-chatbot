import { useState, useEffect, useRef } from "react";
import axios from "axios";
import "./App.css";

const API = "http://localhost:8080";

function App() {

  const [messages, setMessages] = useState([]);
  const [chatHistory, setChatHistory] = useState([]);
  const [prompt, setPrompt] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [currentChatId, setCurrentChatId] = useState(null);
  const [editingChatId, setEditingChatId] = useState(null);
  const [editingTitle, setEditingTitle] = useState("");

  // ==========================================
  // SELECTED FILES
  // MULTIPLE PDF + ONE IMAGE
  // ==========================================

  const [selectedFiles, setSelectedFiles] = useState([]);
  const [previewUrls, setPreviewUrls] = useState([]);

  // File input reference
  const fileInputRef = useRef(null);

  // Chat box reference
  const chatBoxRef = useRef(null);

  // ==========================================
  // CREATE IMAGE PREVIEW URLS
  // ==========================================

  useEffect(() => {

    if (selectedFiles.length === 0) {

      setPreviewUrls([]);

      return;

    }

    const urls = selectedFiles.map((file) => {

      if (file.type.startsWith("image/")) {

        return URL.createObjectURL(file);

      }

      return null;

    });

    setPreviewUrls(urls);

    return () => {

      urls.forEach((url) => {

        if (url) {

          URL.revokeObjectURL(url);

        }

      });

    };

  }, [selectedFiles]);

  // ==========================================
  // AUTO LOAD CHAT HISTORY
  // ==========================================

  useEffect(() => {

    loadChats();

  }, []);

  // ==========================================
  // AUTO SCROLL TO LATEST CONTENT
  // ==========================================

  useEffect(() => {

    if (chatBoxRef.current) {

      chatBoxRef.current.scrollTop =
        chatBoxRef.current.scrollHeight;

    }

  }, [messages, selectedFiles, isLoading]);

  // ==========================================
  // LOAD CHAT HISTORY
  // ==========================================

  const loadChats = async () => {

    try {

      const res = await axios.get(
        `${API}/chats`
      );

      setChatHistory(res.data);

    } catch (err) {

      console.log(err);

    }

  };

  // ==========================================
  // NEW CHAT
  // ==========================================

  const newChat = () => {

    setMessages([]);

    setPrompt("");

    setCurrentChatId(null);

    setSelectedFiles([]);

    setPreviewUrls([]);

    setEditingChatId(null);

    setEditingTitle("");

    if (fileInputRef.current) {

      fileInputRef.current.value = "";

    }

  };

  // ==========================================
  // LOAD OLD CHAT
  // ==========================================

  const loadChat = async (chat) => {

    try {

      const res = await axios.get(
        `${API}/chat/${chat.id}`
      );

      setMessages(res.data);

      setCurrentChatId(chat.id);

      setPrompt("");

      setSelectedFiles([]);

      setPreviewUrls([]);

      if (fileInputRef.current) {

        fileInputRef.current.value = "";

      }

    } catch (err) {

      console.log(err);

    }

  };

  // ==========================================
  // DELETE CHAT
  // ==========================================

  const deleteChat = async (chatId) => {

    console.log("Deleting chat:", chatId);

    try {

      const res = await axios.delete(
        `${API}/chats/${chatId}`
      );

      console.log(
        "Delete response:",
        res
      );

      await loadChats();

      if (currentChatId === chatId) {

        setMessages([]);

        setCurrentChatId(null);

        setSelectedFiles([]);

        setPreviewUrls([]);

      }

    } catch (err) {

      console.log("DELETE ERROR");

      console.log(err);

      if (err.response) {

        console.log(
          err.response.status
        );

        console.log(
          err.response.data
        );

      }

    }

  };

  // ==========================================
  // RENAME CHAT
  // ==========================================

  const renameChat = async (chatId) => {

    const newTitle =
      editingTitle.trim();

    if (!newTitle) {

      return;

    }

    try {

      await axios.put(
        `${API}/chats/${chatId}/title`,
        {
          title: newTitle
        }
      );

      // Refresh chat history
      await loadChats();

      // Exit edit mode
      setEditingChatId(null);

      setEditingTitle("");

    } catch (err) {

      console.log("RENAME ERROR");

      console.log(err);

      if (err.response) {

        console.log(
          "Status:",
          err.response.status
        );

        console.log(
          "Data:",
          err.response.data
        );

      }

    }

  };

  // ==========================================
  // FILE UPLOAD
  // MULTIPLE PDF + ONE IMAGE
  // ==========================================

  const handleFileUpload = (e) => {

    const files =
      Array.from(e.target.files || []);

    if (files.length === 0) {

      return;

    }

    // ==========================================
    // VALIDATE FILE TYPES
    // ==========================================

    const invalidFiles =
      files.filter((file) => {

        const isPdf =
          file.type ===
          "application/pdf";

        const isImage =
          file.type.startsWith(
            "image/"
          );

        return !isPdf && !isImage;

      });

    if (invalidFiles.length > 0) {

      alert(
        "Please select only PDF or image files."
      );

      if (fileInputRef.current) {

        fileInputRef.current.value = "";

      }

      return;

    }

    // ==========================================
    // CHECK HOW MANY IMAGES ARE SELECTED
    // BACKEND CURRENTLY SUPPORTS ONE IMAGE
    // ==========================================

    const existingImageCount =
      selectedFiles.filter(
        (file) =>
          file.type.startsWith("image/")
      ).length;

    const newImageCount =
      files.filter(
        (file) =>
          file.type.startsWith("image/")
      ).length;

    if (
      existingImageCount +
        newImageCount >
      1
    ) {

      alert(
        "You can upload multiple PDFs, but only one image at a time."
      );

      if (fileInputRef.current) {

        fileInputRef.current.value = "";

      }

      return;

    }

    // ==========================================
    // ADD NEW FILES
    // ==========================================

    setSelectedFiles((prevFiles) => [

      ...prevFiles,

      ...files

    ]);

    // ==========================================
    // RESET FILE INPUT
    // SO SAME FILE CAN BE SELECTED AGAIN
    // ==========================================

    if (fileInputRef.current) {

      fileInputRef.current.value = "";

    }

  };

  // ==========================================
  // REMOVE ONE SELECTED FILE
  // ==========================================

  const removeFile = (indexToRemove) => {

    setSelectedFiles((prevFiles) =>

      prevFiles.filter(
        (_, index) =>
          index !== indexToRemove
      )

    );

  };

  // ==========================================
  // ASK AI
  // ==========================================

  const askAI = async () => {

    if (

      (
        !prompt.trim() &&
        selectedFiles.length === 0
      )

      ||

      isLoading

    ) {

      return;

    }

    // ==========================================
    // CURRENT PROMPT
    // ==========================================

    const currentPrompt =
      prompt.trim();

    // ==========================================
    // CREATE USER MESSAGE TEXT
    // ==========================================

    let userText = "";

    // ==========================================
    // ADD ALL SELECTED FILE NAMES
    // ==========================================

    if (selectedFiles.length > 0) {

      selectedFiles.forEach(
        (file, index) => {

          const fileIcon =

            file.type ===
            "application/pdf"

              ? "📄"

              : "🖼️";

          if (index > 0) {

            userText += "\n";

          }

          userText +=
            `${fileIcon} ${file.name}`;

        }
      );

    }

    // ==========================================
    // ADD USER PROMPT
    // ==========================================

    if (currentPrompt) {

      if (userText) {

        userText +=
          "\n\n";

      }

      userText +=
        currentPrompt;

    }

    // ==========================================
    // CREATE USER MESSAGE
    // ==========================================

    const userMessage = {

      sender: "user",

      text: userText,

      files: selectedFiles.map(
        (file, index) => ({

          fileName: file.name,

          fileType: file.type,

          fileUrl:
            file.type.startsWith("image/")
              ? previewUrls[index]
              : null,

        })
      ),

    };

    // ==========================================
    // UPDATE MESSAGES
    // ==========================================

    const updatedMessages = [

      ...messages,

      userMessage

    ];

    setMessages(
      updatedMessages
    );

    setPrompt("");

    setIsLoading(true);

    try {

      // ==========================================
      // CREATE MULTIPART FORM DATA
      // ==========================================

      const formData =
        new FormData();

      // ==========================================
      // ADD PROMPT
      // ==========================================

      formData.append(
        "prompt",
        currentPrompt ||
        "Please analyze the uploaded file(s)."
      );

      // ==========================================
      // ADD CHAT ID
      // ==========================================

      if (currentChatId) {

        formData.append(
          "chatId",
          currentChatId
        );

      }

      // ==========================================
      // ADD ALL FILES
      //
      // IMPORTANT:
      // BACKEND EXPECTS "files"
      // ==========================================

      selectedFiles.forEach(
        (file) => {

          formData.append(
            "files",
            file
          );

        }
      );

      // ==========================================
      // SEND REQUEST TO SPRING BOOT
      // ==========================================

      const res = await axios.post(

        `${API}/chat`,

        formData,

        {
          headers: {

            "Content-Type":
              "multipart/form-data",

          },

        }

      );

      // ==========================================
      // AI RESPONSE
      // ==========================================

      const aiMessage = {

        sender: "ai",

        text: res.data.response,

      };

      // ==========================================
      // SAVE CHAT ID
      // ==========================================

      if (res.data.chatId) {

        setCurrentChatId(
          res.data.chatId
        );

      }

      // ==========================================
      // FINAL MESSAGES
      // ==========================================

      const finalMessages = [

        ...updatedMessages,

        aiMessage,

      ];

      setMessages(
        finalMessages
      );

      // ==========================================
      // CLEAR INPUTS
      // ==========================================

      setPrompt("");

      setSelectedFiles([]);

      setPreviewUrls([]);

      if (fileInputRef.current) {

        fileInputRef.current.value = "";

      }

      // ==========================================
      // REFRESH CHAT HISTORY
      // ==========================================

      await loadChats();

    } catch (error) {

      console.log(
        "FULL ERROR"
      );

      console.log(error);

      if (error.response) {

        console.log(
          "Status:",
          error.response.status
        );

        console.log(
          "Data:",
          error.response.data
        );

      } else {

        console.log(
          error.message
        );

      }

      // ==========================================
      // ERROR MESSAGE
      // ==========================================

      const errorMessage = {

        sender: "ai",

        text:
          "⚠️ " +
          (
            error.response?.data ||
            error.message
          ),

      };

      setMessages([

        ...updatedMessages,

        errorMessage

      ]);

    } finally {

      setIsLoading(false);

    }

  };

  // ==========================================
  // ENTER KEY
  // ==========================================

  const handleKeyDown = (e) => {

    if (

      e.key === "Enter" &&

      !e.shiftKey

    ) {

      e.preventDefault();

      askAI();

    }

  };

  // ==========================================
  // UI
  // ==========================================

  return (

    <div className="app">

      {/* =====================================
          SIDEBAR
      ====================================== */}

      <div className="sidebar">

        <h2>

          🤖 Neon AI

        </h2>

        <button

          className="new-chat-btn"

          onClick={newChat}

        >

          + New Chat

        </button>

        <h3>

          Chat History

        </h3>

        <div className="history">

          {chatHistory.length === 0 ? (

            <p className="no-history">

              No previous chats

            </p>

          ) : (

            chatHistory.map(

              (chat) => (

                <div

                  key={chat.id}

                  className={

                    currentChatId ===
                    chat.id

                      ? "history-item active"

                      : "history-item"

                  }

                  onClick={() => {

                    if (
                      editingChatId !==
                      chat.id
                    ) {

                      loadChat(chat);

                    }

                  }}

                >

                  {/* =====================================
                      CHAT TITLE
                  ====================================== */}

                  {editingChatId ===
                  chat.id ? (

                    <input

                      className="chat-title-input"

                      value={
                        editingTitle
                      }

                      autoFocus

                      onChange={(e) =>

                        setEditingTitle(
                          e.target.value
                        )

                      }

                      onClick={(e) =>

                        e.stopPropagation()

                      }

                      onKeyDown={(e) => {

                        if (
                          e.key ===
                          "Enter"
                        ) {

                          e.preventDefault();

                          renameChat(
                            chat.id
                          );

                        }

                        if (
                          e.key ===
                          "Escape"
                        ) {

                          setEditingChatId(
                            null
                          );

                          setEditingTitle(
                            ""
                          );

                        }

                      }}

                    />

                  ) : (

                    <span>

                      💬 {chat.title}

                    </span>

                  )}

                  {/* =====================================
                      CHAT ACTION BUTTONS
                  ====================================== */}

                  <div className="chat-actions">

                    {/* EDIT BUTTON */}

                    <button

                      className="edit-btn"

                      onClick={(e) => {

                        e.stopPropagation();

                        setEditingChatId(
                          chat.id
                        );

                        setEditingTitle(
                          chat.title || ""
                        );

                      }}

                      title="Rename chat"

                    >

                      ✏️

                    </button>

                    {/* DELETE BUTTON */}

                    <button

                      className="delete-btn"

                      onClick={(e) => {

                        e.stopPropagation();

                        deleteChat(
                          chat.id
                        );

                      }}

                      title="Delete chat"

                    >

                      🗑️

                    </button>

                  </div>

                </div>

              )

            )

          )}

        </div>

      </div>

      {/* =====================================
          MAIN CHAT
      ====================================== */}

      <div className="main">

        {/* =====================================
            TOP BAR
        ====================================== */}

        <div className="top-bar">

          <h1>

            🤖 Neon AI

          </h1>

        </div>

        {/* =====================================
            OUTPUT / CHAT BOX
        ====================================== */}

        <div

          className="chat-box"

          ref={chatBoxRef}

        >

          {/* =====================================
              WELCOME MESSAGE
          ====================================== */}

          {messages.length === 0 &&

            selectedFiles.length === 0 && (

              <div className="welcome">

                <h2>

                  👋 Hello!

                </h2>

                <p>

                  Ask me anything.

                </p>

              </div>

            )}

          {/* =====================================
              CHAT MESSAGES
          ====================================== */}

          {messages.map(

            (msg, index) => (

              <div

                key={index}

                className={

                  msg.sender?.toUpperCase() ===
                  "USER"

                    ? "message user"

                    : "message ai"

                }

              >

                {/* =================================
                    SHOW MULTIPLE SENT FILES
                ================================= */}

                {msg.files &&
                  msg.files.map(
                    (file, fileIndex) => (

                      <div
                        key={fileIndex}
                      >

                        {/* Sent image */}

                        {file.fileUrl &&
                          file.fileType &&
                          file.fileType.startsWith(
                            "image/"
                          ) && (

                            <img

                              src={
                                file.fileUrl
                              }

                              alt={
                                file.fileName
                              }

                              className="sent-image"

                            />

                          )}

                        {/* File name */}

                        {file.fileName && (

                          <div className="sent-file-name">

                            {file.fileType ===
                            "application/pdf"

                              ? "📄"

                              : "🖼️"}{" "}

                            {file.fileName}

                          </div>

                        )}

                      </div>

                    )
                  )}

                {/* =================================
                    OLD MESSAGE FORMAT SUPPORT
                ================================= */}

                {!msg.files &&
                  msg.fileUrl &&
                  msg.fileType &&
                  msg.fileType.startsWith(
                    "image/"
                  ) && (

                    <img

                      src={msg.fileUrl}

                      alt={msg.fileName}

                      className="sent-image"

                    />

                  )}

                {!msg.files &&
                  msg.fileName && (

                    <div className="sent-file-name">

                      {msg.fileType ===
                      "application/pdf"

                        ? "📄"

                        : "🖼️"}{" "}

                      {msg.fileName}

                    </div>

                  )}

                {/* =================================
                    SHOW USER / AI TEXT
                ================================= */}

                {msg.text}

              </div>

            )

          )}

          {/* =====================================
              SELECTED FILE PREVIEWS
          ====================================== */}

          {selectedFiles.length > 0 && (

            <>

              {selectedFiles.map(
                (file, index) => (

                  <div

                    className="file-message-card"

                    key={`${file.name}-${file.lastModified}-${index}`}

                  >

                    {/* =================================
                        IMAGE PREVIEW
                    ================================= */}

                    {file.type.startsWith(
                      "image/"
                    ) ? (

                      <img

                        src={
                          previewUrls[index]
                        }

                        alt={
                          file.name
                        }

                        className="image-preview"

                      />

                    ) : (

                      /* =================================
                         PDF ICON
                      ================================= */

                      <div className="pdf-message-icon">

                        📄

                      </div>

                    )}

                    {/* =================================
                        FILE INFORMATION
                    ================================= */}

                    <div className="pdf-message-info">

                      <div className="pdf-message-name">

                        {file.name}

                      </div>

                      <div className="pdf-message-size">

                        {(
                          file.size /
                          1024 /
                          1024
                        ).toFixed(2)}{" "}
                        MB

                        <span className="pdf-status">

                          • Ready to send

                        </span>

                      </div>

                    </div>

                    {/* =================================
                        REMOVE FILE
                    ================================= */}

                    <button

                      className="remove-pdf-message"

                      onClick={() =>
                        removeFile(index)
                      }

                      title="Remove file"

                    >

                      ✕

                    </button>

                  </div>

                )
              )}

            </>

          )}

          {/* =====================================
              TYPING INDICATOR
          ====================================== */}

          {isLoading && (

            <div className="message ai">

              AI is thinking

              <span className="dots">

                <span>.</span>

                <span>.</span>

                <span>.</span>

              </span>

            </div>

          )}

        </div>

        {/* =====================================
            INPUT AREA
        ====================================== */}

        <div className="input-area">

          {/* =====================================
              TEXT INPUT
          ====================================== */}

          <textarea

            rows="3"

            placeholder={

              selectedFiles.length > 1

                ? "Ask something about these files..."

                : selectedFiles.length === 1

                  ? (

                      selectedFiles[0].type ===
                      "application/pdf"

                        ? "Ask something about this PDF..."

                        : "Ask something about this image..."

                    )

                  : "Ask anything..."

            }

            value={prompt}

            onChange={(e) =>

              setPrompt(
                e.target.value
              )

            }

            onKeyDown={
              handleKeyDown
            }

            disabled={isLoading}

          />

          {/* =====================================
              INPUT BOTTOM
          ====================================== */}

          <div className="input-bottom">

            <div className="input-actions">

              {/* =================================
                  FILE UPLOAD
              ================================= */}

              <div>

                <input

                  ref={fileInputRef}

                  type="file"

                  multiple

                  accept=".pdf,application/pdf,image/*"

                  onChange={
                    handleFileUpload
                  }

                  style={{

                    display: "none"

                  }}

                  id="file-upload"

                />

                <label

                  htmlFor="file-upload"

                  className="upload-btn"

                  title="Upload multiple PDFs or one image"

                >

                  +

                </label>

              </div>

              {/* =================================
                  SEND BUTTON
              ================================= */}

              <button

                className="send-btn"

                onClick={askAI}

                disabled={

                  isLoading ||

                  (

                    !prompt.trim() &&

                    selectedFiles.length === 0

                  )

                }

              >

                {isLoading

                  ? "Thinking..."

                  : "Send ➤"

                }

              </button>

            </div>

          </div>

        </div>

      </div>

    </div>

  );

}

export default App;