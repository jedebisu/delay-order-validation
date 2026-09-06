let memoryRequests = [
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

module.exports = (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Content-Type', 'application/json');

  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }

  if (req.method === 'GET') {
    return res.status(200).json(memoryRequests);
  }

  if (req.method === 'POST') {
    const { workordernumber, partner, channel, with3waynotes, person3wayed, threewaynotes } = req.body || {};
    const newRequest = {
      id: memoryRequests.length + 1,
      timestamp: new Date().toISOString(),
      workordernumber, partner, channel, delay: "", delaydateandtime: null,
      with3waynotes: Boolean(with3waynotes), person3wayed: person3wayed || "", threewaynotes: threewaynotes || "",
      status: "PENDING", bderemarks: "", approverEmail: null, actionTakenAt: null
    };
    memoryRequests.unshift(newRequest);
    return res.status(201).json(newRequest);
  }

  return res.status(405).json({ error: "Method Not Allowed" });
};
