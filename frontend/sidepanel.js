document.addEventListener('DOMContentLoaded', () => {
    chrome.storage.local.get(['researchNotes'], function(result) {
        if (result.researchNotes) {
            document.getElementById('notes').value = result.researchNotes
        }
    });

    document.getElementById('summarizeBtn').addEventListener('click', summarizeText)
    document.getElementById('saveNotesBtn').addEventListener('click', saveNotes)
})

const summarizeText = async () => {
    try {
        const [tab] = await chrome.tabs.query({ active:true, currentWindow: true })
        const [{ result }] = await chrome.scripting.executeScript({
            target: { tabId: tab.id },
            func: () => window.getSelection().toString()
        });

        if (!result) {
            showResult("Please select some text first")
            return;
        }

        const response = await fetch('http://localhost:8080/api/readless/process', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: result, operation: 'qa'})
        })

        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }

        const text = await response.text();
        showResult(text.replace(/\n/g, '<br>'))
    } catch (error) {
        showResult('ERROR: ' + error.message)
    }
}

const saveNotes = async () => {
    const notes = document.getElementById('notes').value;
    chrome.storage.local.set({ 'researchNotes': notes }, function() {
        alert('Notes saved successfully')
    });
}

const showResult = (content) => {
    document.querySelector('.results').innerHTML =
  `<div class="result-item"><div class="result-content">${content}</div></div>`;
  };