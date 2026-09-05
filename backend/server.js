const express = require('express');
const cors = require('cors');
const app = express();
const PORT = 5001;

app.use(cors());
app.use(express.json());

let requests = [
  {
    id: 1,
    timestamp: new Date().toISOString(),
    workordernumber: "WO-99482",
    partner: "Ebisu",
    channel: "IPG",
    delay: "",
    delaydateandtime: null,
    with3waynotes: true,
    person3wayed: "John Doe (Subscriber)",
    threewaynotes: "Subscriber requested delay due to site renovation. Numbers: 09171234567.",
    status: "PENDING",
    bderemarks: "",
    approverEmail: null,
    actionTakenAt: null
  }
];

app.get('/api/requests', (req, res) => res.json(requests));

app.post('/api/requests', (req, res) => {
  const { workordernumber, partner, channel, with3waynotes, person3wayed, threewaynotes } = req.body;
  const newRequest = {
    id: requests.length + 1,
    timestamp: new Date().toISOString(),
    workordernumber, partner, channel, delay: "", delaydateandtime: null,
    with3waynotes: Boolean(with3waynotes), person3wayed: person3wayed || "", threewaynotes: threewaynotes || "",
    status: "PENDING", bderemarks: "", approverEmail: null, actionTakenAt: null
  };
  requests.unshift(newRequest);
  res.status(201).json({ message: "Request created", request: newRequest });
});

app.patch('/api/requests/:id/action', (req, res) => {
  const { id } = req.params;
  const { status, bderemarks, delaydateandtime, approverEmail } = req.body;
  const item = requests.find(r => r.id === parseInt(id));
  if (!item) return res.status(404).json({ error: "Not found" });
  item.status = status;
  item.bderemarks = bderemarks || "";
  item.delaydateandtime = delaydateandtime || null;
  item.approverEmail = approverEmail || "cpjuezan@globe.com.ph";
  item.actionTakenAt = new Date().toISOString();
  res.json({ message: `Updated to ${status}`, request: item });
});

app.listen(PORT, () => console.log(`✅ Server running on http://localhost:${PORT}`));
