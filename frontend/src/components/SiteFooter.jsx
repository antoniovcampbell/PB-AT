import '../styles/footer.css'

function SiteFooter() {
  return (
    <footer className="footer">
      <span>PB market</span>
       <span>Consulte preços, estoque e avaliações antes de comprar.</span>
       <span>© {new Date().getFullYear()} PB Market</span>
    </footer>
  )
}

export default SiteFooter
